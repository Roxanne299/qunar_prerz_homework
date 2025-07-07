package com.waaar.rpc.rpc;

import com.waaar.rpc.service.impl.MathServiceImpl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RPCServer {

    // 模拟的注册中心
    private final Map<String, Object> serviceRegistry = new ConcurrentHashMap<>();

    /**
     * 注册服务
     *
     * @param serviceName
     * @param serviceImpl
     */
    public void registerService(String serviceName, Object serviceImpl) {
        serviceRegistry.put(serviceName, serviceImpl);
        System.out.println("服务端注册服务：" + serviceName + "-->" + serviceImpl.getClass().getName());
    }

    /**
     * 启动服务
     *
     * @param port
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void start(int port)  {
        try (
                Selector selector = Selector.open();
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()
        ) {
            serverSocketChannel.configureBlocking(false);
            System.out.println("服务端已经启动，端口" + port + ".......");
            serverSocketChannel.socket().bind(new InetSocketAddress(port));

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            registerService("MathService", new MathServiceImpl());
            // 服务端主循环 - 持续监听和处理客户端请求
            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    // 处理新连接事件
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = server.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                    // 处理数据可读事件
                    else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);

                        int len = socketChannel.read(buffer);
                        if (len == -1) {
                            // 客户端关闭连接，服务端应关闭通道
                            socketChannel.close();
                            System.out.println("客户端已关闭连接");
                        } else if (len > 0) {
                            buffer.flip();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            String msg = new String(bytes).trim();
                            if (!msg.isEmpty()) {
                                System.out.println("接受到数据: " + msg);
                                String s = handleRequest(msg);
                                System.out.println("返回结果 " + s);
                                buffer.clear();
                                buffer.put(s.getBytes());
                                buffer.flip();
                                socketChannel.write(buffer);
                            }
                        }

                    }
                }
                // 清除这些selection
                selectionKeys.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    // 目前版本不能严格对方法参数类型进行准确匹配

    /**
     * 处理消费者发送的请求
     *
     * @param request
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public String handleRequest(String request) throws InvocationTargetException, IllegalAccessException {
        RPCRequest rpcRequest = RPCProtocol.decodeRequest(request);
        Object serviceImpl = serviceRegistry.get(rpcRequest.getServiceName());
        if (serviceImpl == null) {
            return String.valueOf(rpcRequest.getRequestId()) + "|error=该服务未注册或者不存在";
        }

        Class<?> serviceClass = serviceImpl.getClass();
        // 寻找对应的方法
        Method[] declaredMethods = serviceClass.getDeclaredMethods();
        Method serviceMethod = null;

        for (Method method : declaredMethods) {
            String methodName = method.getName();
            if (methodName.equals(rpcRequest.getMethodName())) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (rpcRequest.getParameters() == null && parameterTypes.length == 0 || parameterTypes.length == rpcRequest.getParameters().length) {
                    serviceMethod = method;
                    break;
                }

            }
        }

        if (serviceMethod == null) {
            return String.valueOf(rpcRequest.getRequestId()) + "|error=该方法不存在";
        }

        Object result = null;
        try {
            result = serviceMethod.invoke(serviceImpl, convertParameters(rpcRequest.getParameters(), serviceMethod.getParameterTypes()));
        } catch (Exception e) {
            return String.valueOf(rpcRequest.getRequestId()) + "|error=" + e.getMessage();
        }


        return RPCProtocol.encodeResponse(rpcRequest.getRequestId(), result, null);


    }

    /**
     * 转换参数类型
     *
     * @param parameters
     * @param parameterTypes
     * @return
     */
    private Object[] convertParameters(Object[] parameters, Class<?>[] parameterTypes) {
        if (parameters.length == 0) {
            return new Object[0];
        }
        Object[] result = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class type = parameterTypes[i];
            Object o = convertSingleParameter((String) parameters[i], type);
            result[i] = o;
        }
        return result;
    }


    /**
     * 单个参数转换 - 支持基本类型
     *
     * @param paramStr
     * @param targetType
     * @return
     */
    private Object convertSingleParameter(String paramStr, Class<?> targetType) {
        if (paramStr == null || "null".equalsIgnoreCase(paramStr)) {
            return null;
        }

        // 去除前后空格
        paramStr = paramStr.trim();

        // String 类型直接返回
        if (targetType == String.class) {
            return paramStr;
        }

        // 基本类型和包装类型
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(paramStr);
        }

        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(paramStr);
        }

        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(paramStr);
        }

        if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(paramStr);
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(paramStr) ||
                    "1".equals(paramStr) ||
                    "yes".equalsIgnoreCase(paramStr) ||
                    "true".equalsIgnoreCase(paramStr);
        }

        if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(paramStr);
        }

        if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(paramStr);
        }

        if (targetType == char.class || targetType == Character.class) {
            return paramStr.length() > 0 ? paramStr.charAt(0) : '\0';
        }
        // 默认尝试使用构造函数
        try {
            return targetType.getConstructor(String.class).newInstance(paramStr);
        } catch (Exception e) {
            // 最后尝试 valueOf 方法
            try {
                Method valueOfMethod = targetType.getMethod("valueOf", String.class);
                return valueOfMethod.invoke(null, paramStr);
            } catch (Exception ex) {
                // 如果都失败了，返回字符串
                throw new RuntimeException("警告: 无法转换参数 " + paramStr + " 到类型 " + targetType.getSimpleName() + "，使用字符串形式");
            }
        }
    }

}
