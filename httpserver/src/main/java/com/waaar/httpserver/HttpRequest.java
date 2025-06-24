package com.waaar.httpserver;

import java.util.Map;

public class HttpRequest {
    private final String method;
    private final String uri;
    private final Map<String, String> headers;
    private final String body;
    private final boolean KeepAlive;
    public HttpRequest(String method, String uri, Map<String, String> headers, String body, boolean keepAlive) {
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.body = body;
        this.KeepAlive = keepAlive;
    }
    public String getMethod() {
        return method;
    }
    public String getUri() {
        return uri;
    }
    public Map<String, String> getHeaders() {
        return headers;
    }
    public String getBody() {
        return body;
    }
    public boolean isKeepAlive() {
        return KeepAlive;
    }
}
