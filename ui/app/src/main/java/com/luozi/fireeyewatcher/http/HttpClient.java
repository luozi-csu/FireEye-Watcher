package com.luozi.fireeyewatcher.http;

import android.util.Log;

import com.luozi.fireeyewatcher.model.HttpRequest;
import com.luozi.fireeyewatcher.model.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpClient {
    private static final String LOG_TAG = "HTTP_CLIENT";
    private String protocol;
    private String host;
    private int port;
    private int readTimeout;
    private int connectTimeout;
    private HttpResponseBuffer responseBuffer;

    public HttpClient(String protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.readTimeout = 5000;
        this.connectTimeout = 5000;
        this.responseBuffer = new HttpResponseBuffer();
    }

    public void setReadTimeout(int timeout) {
        this.readTimeout = timeout;
    }

    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }

    private class HttpResponseBuffer {
        private HttpResponse response;
        private boolean valueSet;

        public synchronized HttpResponse get() {
            if (!valueSet) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            valueSet = false;
            notify();
            return response;
        }

        public synchronized void put(HttpResponse response) {
            if (valueSet) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            this.response = response;
            valueSet = true;
            notify();
        }
    }

    private class DoRequestThread extends Thread {
        private HttpRequest request;
        private HttpResponseBuffer buffer;

        public DoRequestThread(HttpRequest request, HttpResponseBuffer buffer) {
            super();
            this.request = request;
            this.buffer = buffer;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) request.url.openConnection();
                conn.setRequestMethod(request.method);
                conn.setReadTimeout(readTimeout);
                conn.setConnectTimeout(connectTimeout);
                conn.setDoInput(true);

                if (request.method.toLowerCase().equals(HttpRequest.POST)) {
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                }

                if (request.header != null) {
                    for (Map.Entry<String, String[]> entry : request.header.entrySet()) {
                        String[] values = entry.getValue();
                        conn.setRequestProperty(entry.getKey(), String.join(",", values));
                    }
                }

                Log.d(LOG_TAG, String.format("do request: [%s] %s", request.method, request.url));

                if (request.data != null) {
                    OutputStream out = conn.getOutputStream();
                    out.write(request.data.toString().getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                StringBuffer strBuffer = new StringBuffer();
                String strRead = null;
                while ((strRead = reader.readLine()) != null) {
                    strBuffer.append(strRead);
                }
                reader.close();

                JSONObject json = new JSONObject(strBuffer.toString());
                HttpResponse response = new HttpResponse(json.getInt("status_code"),
                        json.getString("data"), json.getString("desc"));

                buffer.put(response);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
            }
        }
    }

    public HttpResponse doRequest(String method, String path, Map<String, String[]> header, Map<String, String[]> params, Object data) {
        HttpRequest request = null;
        try {
            request = prepareRequest(method, path, header, params, data);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        new DoRequestThread(request, this.responseBuffer).start();

        HttpResponse response = responseBuffer.get();

        if (response != null) {
            if (response.statusCode >= 400) {
                Log.e(LOG_TAG, String.format("request error: [%s] %s", method, path));
            } else if (response.statusCode >= 500) {
                Log.i(LOG_TAG, String.format("remote server error: [%s] %s", method, path));
            }
        }

        return response;
    }

    private HttpRequest prepareRequest(String method, String path, Map<String, String[]> header, Map<String, String[]> params, Object data) throws MalformedURLException, UnsupportedEncodingException {
        String root = String.format("%s://%s:%d", protocol, host, port);

        if (path != null && path.length() > 0) {
            if (path.charAt(0) != '/') {
                path = "/".concat(path);
            }
            root = root.concat(path);
        }

        if (params != null) {
            String paramStr = "?";
            List<String> paramList = new ArrayList<>();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                String[] values = entry.getValue();
                String valueStr = String.join(",", values);
                paramList.add(String.format(entry.getKey() + "=" + valueStr));
            }
            paramStr = paramStr.concat(String.join("&", paramList));
            URLEncoder.encode(paramStr, "UTF-8");
        }

        URL url = new URL(root);
        Log.d(LOG_TAG, String.format("prepare request url: %s", url));

        return new HttpRequest(method, url, header, data);
    }
}
