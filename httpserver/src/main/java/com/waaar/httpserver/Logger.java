package com.waaar.httpserver;

import java.util.Map;

public class Logger {
    public static void logRequest(String clientIP, HttpRequest request, long startTime) {
//        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(startTime));


        // 输出请求头信息
        System.out.println("  请求头:");
        for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            System.out.println("    " + header.getKey() + ": " + header.getValue());
        }

        // 输出请求体信息
        if (!request.getBody().isEmpty()) {
            System.out.println("  请求体: " + request.getBody());
        }
    }
}
