package com.luozi.fireeyewatcher.http;

import android.util.Log;

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
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpClient {
    private static final String LOG_TAG = "HTTP_CLIENT";
    public int readTimeout;
    public int connectTimeout;

    public HttpClient() {
        this.readTimeout = 5000;
        this.connectTimeout = 5000;
    }

    public void setReadTimeout(int timeout) {
        this.readTimeout = timeout;
    }

    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }

    public HttpResponse doRequest(HttpRequest request) {
        HttpURLConnection conn = null;
        HttpResponse response = null;
        try {
            conn = (HttpURLConnection) request.url.openConnection();
            conn.setRequestMethod(request.method);
            conn.setReadTimeout(readTimeout);
            conn.setConnectTimeout(connectTimeout);
            conn.setDoInput(true);

            if (request.method.equals(Common.POST) || request.method.equals(Common.PUT)) {
                conn.setDoOutput(true);
                conn.setUseCaches(false);
            }

            if (request.header != null) {
                for (Map.Entry<String, String[]> entry : request.header.entrySet()) {
                    String[] values = entry.getValue();
                    conn.setRequestProperty(entry.getKey(), String.join(",", values));
                }
            }

            if (request.data != null) {
                OutputStream out = conn.getOutputStream();
                out.write(request.data.toString().getBytes(StandardCharsets.UTF_8));
                out.flush();
                Log.d(LOG_TAG, String.format("do request: [%s] %s --data %s", request.method, request.url, request.data));
            } else {
                Log.d(LOG_TAG, String.format("do request: [%s] %s", request.method, request.url));
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
            response = new HttpResponse(json.getInt("status_code"),
                    json.getString("data"), json.getString("desc"));
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

        return response;
    }
}
