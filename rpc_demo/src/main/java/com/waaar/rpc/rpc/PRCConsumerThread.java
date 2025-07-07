package com.waaar.rpc.rpc;

import com.waaar.rpc.service.impl.MathServiceImpl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class PRCConsumerThread implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            sendRequest();
        }
    }

    public void sendRequest() {
        try (
                SocketChannel socketChannel = SocketChannel.open();
        ) {
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8081));

            // 1. 非阻塞模式下要判断连接是否完成
            while (!socketChannel.finishConnect()) {
                Thread.sleep(1);
            }

            // 2. 写数据前 flip
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            byte[] bytes = "MathService|multiply|1,2".getBytes();
            buffer.put(bytes);
            buffer.flip(); // 切换到读模式，准备写

            // 3. write 可能没写完，要循环
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
            buffer.clear();

            // 4. 可以考虑读取服务器响应
            while (true) {
                int len = socketChannel.read(buffer);
                if (len > 0) {
                    buffer.flip();
                    byte[] bytes1 = new byte[buffer.remaining()];
                    buffer.get(bytes1);
                    String msg = new String(bytes1).trim();
                    if (!msg.isEmpty()) {
                        RPCResponse response = RPCProtocol.decodeResponse(msg);
                        if (msg.contains("result")) {
                            System.out.println("返回结果： " + response.getResult());
                        } else {
                            System.out.println("发生错误： " + response.getError());
                        }
                        break;
                    }
                } else if (len == -1) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
