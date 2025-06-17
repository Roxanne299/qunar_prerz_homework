package com.waaar.httpserver;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class HttpHandler implements Runnable {
    private final Socket client;

    public HttpHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))
        ) {
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String path = parts[1];

            Map<String, String> headers = new HashMap<>();
            String line;
            while (!(line = reader.readLine()).isEmpty()) {
                String[] header = line.split(": ");
                if (header.length == 2)
                    headers.put(header[0], header[1]);
            }

            StringBuilder body = new StringBuilder();
            if ("POST".equalsIgnoreCase(method)) {
                while (reader.ready()) {
                    body.append((char) reader.read());
                }
            }

            // 日志
            Logger.logRequest(client, path, headers.get("User-Agent"));

            if ("/hello".equals(path)) {
                writeResponse(writer, 200, "text/plain", "Hello, World!");
            } else if ("/post".equals(path) && "POST".equals(method)) {
                writeResponse(writer, 200, "text/plain", "POST received: " + body);
            } else {
                writeResponse(writer, 404, "text/plain", "Not Found");
            }

            writer.flush();
            if (!"keep-alive".equalsIgnoreCase(headers.get("Connection"))) {
                client.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeResponse(BufferedWriter writer, int statusCode, String contentType, String body) throws IOException {
        writer.write("HTTP/1.1 " + statusCode + " \r\n");
        writer.write("Content-Type: " + contentType + "\r\n");
        writer.write("Content-Length: " + body.getBytes().length + "\r\n");
        writer.write("Connection: close\r\n\r\n");
        writer.write(body);
    }
}
