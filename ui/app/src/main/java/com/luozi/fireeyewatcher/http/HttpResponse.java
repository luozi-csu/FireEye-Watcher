package com.luozi.fireeyewatcher.http;

import androidx.annotation.NonNull;

public class HttpResponse {
    public int statusCode;
    public Object data;
    public String desc;

    public HttpResponse(int statusCode, Object data, String desc) {
        this.statusCode = statusCode;
        this.data = data;
        this.desc = desc;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("{\"status_code\":%d,\"data\":%s,\"desc\":%s}", statusCode, data, desc);
    }
}
