package com.luozi.fireeyewatcher.model;

import java.net.URL;
import java.util.Map;

public class HttpRequest {
    public String method;
    public URL url;
    public Map<String, String[]> header;
    public Object data;
    public static final String GET = "get";
    public static final String POST = "post";
    public static final String PUT = "put";
    public static final String DELETE = "delete";

    public HttpRequest(String method, URL url, Map<String, String[]> header, Object data) {
        this.method = method;
        this.url = url;
        this.header = header;
        this.data = data;
    }
}
