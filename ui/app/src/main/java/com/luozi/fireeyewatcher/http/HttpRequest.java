package com.luozi.fireeyewatcher.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    public String method;
    public String path;
    public URL url;
    public Map<String, String[]> params;
    public Map<String, String[]> header;
    public Object data;

    public HttpRequest(String method, String path, Map<String, String[]> params, Map<String, String[]> header, Object data) {
        this.method = method;
        this.path = path;
        this.params = params;
        this.header = header;
        this.data = data;

        if (params != null) {
            List<String> paramList = new ArrayList<>();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                String[] values = entry.getValue();
                String valueStr = String.join(",", values);
                paramList.add(entry.getKey() + "=" + valueStr);
            }
            String paramStr = "?" + String.join("&", paramList);
            this.path += paramStr;
        }

        try {
            this.url = new URL(path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
