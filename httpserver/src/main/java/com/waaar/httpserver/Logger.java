package com.waaar.httpserver;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public static void logRequest(Socket client, String path, String ua) {
        String ip = client.getInetAddress().getHostAddress();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.printf("[%s] IP: %s Path: %s UA: %s%n", time, ip, path, ua);
    }
}
