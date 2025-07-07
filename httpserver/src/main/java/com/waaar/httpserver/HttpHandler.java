package com.waaar.httpserver;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpHandler implements Runnable {
    private final Socket client;

    public HttpHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        String clientAddress = this.client.getInetAddress().toString();
        System.out.println("开始处理来自 " + clientAddress + " 的连接");


        try (BufferedReader in = new BufferedReader(new InputStreamReader(this.client.getInputStream())); PrintWriter out = new PrintWriter(new OutputStreamWriter(this.client.getOutputStream()));) {
            // 设置Socket超时，避免无限阻塞
            this.client.setSoTimeout(30000); // 30秒超时

            HttpRequest request = parseHttpRequest(in);

            // 添加 null 检查，避免空指针异常
            if (request == null) {
                System.err.println("解析HTTP请求失败，请求为空 - 客户端: " + clientAddress);
                HttpResponse errorResponse = new HttpResponse(400, "text/plain; charset=UTF-8", "Bad Request");
                writeResponse(out, errorResponse, false);
                this.client.close();
                return;
            }

            System.out.println("成功解析请求: " + request.getMethod() + " " + request.getUri() + " - 客户端: " + clientAddress);

            // 记录请求日志
            Logger.logRequest(clientAddress, request, System.currentTimeMillis());
            HttpResponse response = handleHttpRequest(request, out);

            writeResponse(out, response, request.isKeepAlive());
            if (!request.isKeepAlive()) {
                System.out.println("关闭连接 - 客户端: " + clientAddress);
                this.client.close();
            } else {
                System.out.println("保持连接 - 客户端: " + clientAddress);
            }

        } catch (Exception e) {
            System.err.println("处理HTTP请求时发生异常 - 客户端: " + clientAddress + " - 异常: " + e.getMessage());
            e.printStackTrace();
            try {
                this.client.close();
            } catch (IOException closeException) {
                System.err.println("关闭客户端连接时出错: " + closeException.getMessage());
            }
        } finally {

        }
    }

    private void writeResponse(PrintWriter out, HttpResponse response, boolean keepAlive) throws IOException {
        // 响应行
        out.println("HTTP/1.1 " + response.getStatusCode() + " " + getStatusText(response.getStatusCode()));

        // 响应头
        out.println("Content-Type: " + response.getContentType());
        out.println("Content-Length: " + response.getBody().getBytes("UTF-8").length);
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Methods: GET, POST, OPTIONS");
        out.println("Access-Control-Allow-Headers: Content-Type, Authorization");

        if (keepAlive) {
            out.println("Connection: keep-alive");
        } else {
            out.println("Connection: close");
        }

        out.println(); // 空行分隔头和体
        out.println(response.getBody());
        out.flush();
    }

    /**
     * 获取状态码对应的状态文本
     */
    private String getStatusText(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 400:
                return "Bad Request";
            case 404:
                return "Not Found";
            case 405:
                return "Method Not Allowed";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown";
        }
    }

    private HttpResponse handleHttpRequest(HttpRequest request, Writer out) {
        String path = request.getUri();
        String method = request.getMethod();

        if ("/hello".equals(path)) {
            if ("GET".equalsIgnoreCase(method)) {
                return new HttpResponse(200, "text/plain; charset=UTF-8", "Hello, World! (GET请求)");
            } else if ("POST".equalsIgnoreCase(method)) {
                String responseBody = "Hello, World! (POST请求)\n\n收到的请求体内容:\n" + request.getBody();
                return new HttpResponse(200, "text/plain; charset=UTF-8", responseBody);
            } else {
                return new HttpResponse(405, "text/plain; charset=UTF-8", "方法不被允许");
            }
        }

        // 处理/api/echo路径 - 回显请求数据，支持不同Content-Type
        if ("/api/echo".equals(path)) {
            return handleEchoRequest(request);
        }

        // 处理根路径
        if ("/".equals(path)) {
            String html = generateWelcomePage(request);
            return new HttpResponse(200, "text/html; charset=UTF-8", html);
        }

        // 404错误
        return new HttpResponse(404, "text/html; charset=UTF-8", "<html><body><h1>404 Not Found</h1><p>请求的资源不存在</p></body></html>");
    }

    private HttpResponse handleEchoRequest(HttpRequest request) {
        String contentType = request.getHeaders().getOrDefault("content-type", "text/plain").toLowerCase();

        if (contentType.contains("application/json")) {
            // 如果请求是JSON，返回JSON格式的回显
            String jsonResponse = "{\"echo\":\"JSON数据回显\",\"received_body\":\"" + request.getBody() + "\",\"content_type\":\"" + contentType + "\"}";
            return new HttpResponse(200, "text/plain; charset=UTF-8", jsonResponse);
        } else if (contentType.contains("application/x-www-form-urlencoded")) {
            // 如果是表单数据，返回HTML格式的回显
            String htmlResponse = request.getBody();
            return new HttpResponse(200, "text/plain; charset=UTF-8", htmlResponse);
        } else if (contentType.contains("multipart/form-data")) {
            String htmlResponse = parseMultiFormData(request.getBody());

            return new HttpResponse(200, "text/plain; charset=UTF-8", htmlResponse);
        } else {
            // 默认返回纯文本
            String textResponse = "请求回显:\nContent-Type: " + contentType + "\n请求体: " + request.getBody();
            return new HttpResponse(200, "text/plain; charset=UTF-8", textResponse);
        }
    }

    private String parseMultiFormData(String body) {
        StringBuilder result = new StringBuilder();

        // 直接使用正则表达式匹配整个模式
        Pattern pattern = Pattern.compile("name=\"([^\"]+)\"\\s*\\n\\s*\\n([^-]+?)(?=\\n*-{6})", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(body);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();

            if (result.length() > 0) {
                result.append("\n");
            }
            result.append(key).append("=").append(value);
        }

        return result.toString();
    }

    /**
     * 生成欢迎页面
     */
    private String generateWelcomePage(HttpRequest request) {
        return "<!DOCTYPE html>" + "<html><head><title>Simple HTTP Server</title></head>" + "<body>" + "<h1>欢迎使用简单HTTP服务器</h1>" + "<p>可用的路径:</p>" + "<ul>" + "<li><a href='/hello'>GET /hello</a> - 返回Hello World</li>" + "</ul>" +

                "<h3>测试不同Content-Type的POST请求:</h3>" +

                "<h4>1. 普通表单提交 (application/x-www-form-urlencoded)</h4>" + "<form method='post' action='/hello'>" + "姓名: <input type='text' name='name' value='张三'><br><br>" + "年龄: <input type='number' name='age' value='25'><br><br>" + "<input type='submit' value='发送表单数据'>" + "</form>" +

                "<h4>2. 回显测试 (支持多种Content-Type)</h4>" + "<form method='post' action='/api/echo'>" + "消息: <input type='text' name='message' value='Hello Server'><br><br>" + "<input type='submit' value='发送到回显接口'>" + "</form>" +

                "<h4>3. 测试JSON数据</h4>" + "<pre>curl -X POST http://localhost:8080/api/echo \\\n" + "  -H \"Content-Type: application/json\" \\\n" + "  -d '{\"name\":\"张三\",\"age\":25,\"city\":\"北京\"}'</pre>" +

                "<h4>4. 测试纯文本数据</h4>" + "<pre>curl -X POST http://localhost:8080/api/echo \\\n" + "  -H \"Content-Type: text/plain\" \\\n" + "  -d '这是一段纯文本消息'</pre>" +

                "</body></html>";
    }

    private HttpRequest parseHttpRequest(BufferedReader in) throws IOException {
        // 解析请求行
        String requestLine = null;
        try {
            requestLine = in.readLine();
        } catch (IOException e) {
            System.err.println("读取请求行时发生IO异常: " + e.getMessage());
            return null;
        }

        if (requestLine == null || requestLine.trim().isEmpty()) {
            System.err.println("请求行为空或null，可能是客户端提前关闭连接");
            return null;
        }

        System.out.println("收到请求行: " + requestLine);

        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 3) {
            System.err.println("请求行格式错误: " + requestLine);
            return null;
        }

        String method = requestParts[0];
        String uri = requestParts[1];
        String httpVersion = requestParts[2];

        // 解析请求头
        Map<String, String> headers = readHeadersOptimized(in);

        // 解析请求体 - 现在使用同一个BufferedReader
        String body = parseRequestBody(method, headers, in);

        // 判断是否保持连接
        boolean keepAlive = "keep-alive".equalsIgnoreCase(headers.get("connection"));

        return new HttpRequest(method, uri, headers, body, keepAlive);
    }

    /**
     * 优化后的请求头读取方法，避免阻塞
     */
    private Map<String, String> readHeadersOptimized(BufferedReader in) throws IOException {
        Map<String, String> headers = new LinkedHashMap<>();
        String line;

        while ((line = in.readLine()) != null) {
            // 空行表示头部结束
            if (line.trim().isEmpty()) {
                break;
            }

            // 解析header行
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String key = line.substring(0, colonIndex).trim().toLowerCase();
                String value = line.substring(colonIndex + 1).trim();
                headers.put(key, value);
            }
        }

        return headers;
    }

    /**
     * 修复后的请求体解析方法 - 使用同一个BufferedReader
     */
    private String parseRequestBody(String method, Map<String, String> headers, BufferedReader in) throws IOException {
        if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method)) {
            return "";
        }

        if (!headers.containsKey("content-length")) {
            System.out.println("POST/PUT请求缺少Content-Length头部");
            return "";
        }

        int contentLength;
        try {
            contentLength = Integer.parseInt(headers.get("content-length"));
        } catch (NumberFormatException e) {
            System.err.println("无效的Content-Length: " + headers.get("content-length"));
            return "";
        }

        if (contentLength <= 0) {
            System.out.println("Content-Length为0或负数: " + contentLength);
            return "";
        }

        System.out.println("开始读取请求体，预期长度: " + contentLength + " 字节");

        // 使用BufferedReader的read方法读取指定长度的字符
        char[] bodyChars = new char[contentLength];
        int totalRead = 0;

        while (totalRead < contentLength) {
            int read = in.read(bodyChars, totalRead, contentLength - totalRead);
            if (read == -1) {
                System.err.println("连接意外关闭，已读取: " + totalRead + "/" + contentLength + " 字符");
                break;
            }
            totalRead += read;
            System.out.println("已读取: " + totalRead + "/" + contentLength + " 字符");
        }

        if (totalRead != contentLength) {
            System.err.println("警告：读取的字符数不匹配，期望: " + contentLength + "，实际: " + totalRead);
        }

        String rawBody = new String(bodyChars, 0, totalRead);
        String contentType = headers.getOrDefault("content-type", "").toLowerCase();

        System.out.println("成功读取请求体，Content-Type: " + contentType);
        System.out.println("请求体内容预览: " + (rawBody.length() > 100 ? rawBody.substring(0, 100) + "..." : rawBody));

        // 根据Content-Type处理请求体
        if (contentType.contains("application/json")) {
            return formatJsonBody(rawBody);
        } else if (contentType.contains("application/x-www-form-urlencoded")) {
            return parseFormData(rawBody);
        } else if (contentType.contains("text/plain")) {
            return "纯文本内容: " + rawBody;
        } else if (contentType.contains("multipart/form-data")) {
            return "文件上传内容 (长度: " + rawBody.length() + " 字符):\n" + rawBody;
        } else {
            return "原始数据: " + rawBody;
        }
    }

    private String parseFormData(String rawBody) {
        StringBuilder formatted = new StringBuilder("表单数据:\n");
        String[] pairs = rawBody.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    String key = URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = URLDecoder.decode(keyValue[1], "UTF-8");
                    formatted.append("  ").append(key).append(" = ").append(value).append("\n");
                } catch (UnsupportedEncodingException e) {
                    formatted.append("  ").append(keyValue[0]).append(" = ").append(keyValue[1]).append("\n");
                }
            }
        }
        return formatted.toString();
    }

    private String formatJsonBody(String rawBody) {
        return "JSON数据:\n" + rawBody;
    }
}