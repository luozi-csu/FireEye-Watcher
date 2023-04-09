package com.luozi.fireeyewatcher.http;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
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

    public HttpResponse doFileUpload(String url, Map<String, String> map,
                                          String filePath, byte[] body_data, String charset) {
        // 设置三个常用字符串常量：换行、前缀、分界线（NEWLINE、PREFIX、BOUNDARY）；
        final String NEWLINE = "\r\n";
        final String PREFIX = "--";
        final String BOUNDARY = "#";
        HttpURLConnection httpConn = null;
        HttpResponse response = null;
        BufferedInputStream bis = null;
        DataOutputStream dos = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // 实例化URL对象。调用URL有参构造方法，参数是一个url地址；
            URL urlObj = new URL(url);
            // 调用URL对象的openConnection()方法，创建HttpURLConnection对象；
            httpConn = (HttpURLConnection) urlObj.openConnection();
            // 调用HttpURLConnection对象setDoOutput(true)、setDoInput(true)、setRequestMethod("POST")；
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("POST");
            // 设置Http请求头信息；（Accept、Connection、Accept-Encoding、Cache-Control、Content-Type、User-Agent）
            httpConn.setUseCaches(false);
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Accept", "*/*");
            httpConn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            httpConn.setRequestProperty("Cache-Control", "no-cache");
            httpConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + BOUNDARY);
            httpConn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)");
            // 调用HttpURLConnection对象的connect()方法，建立与服务器的真实连接；
            httpConn.connect();

            // 调用HttpURLConnection对象的getOutputStream()方法构建输出流对象；
            dos = new DataOutputStream(httpConn.getOutputStream());
            // 获取表单中上传控件之外的控件数据，写入到输出流对象（根据HttpWatch提示的流信息拼凑字符串）；
            if (map != null && !map.isEmpty()) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String key = entry.getKey();
                    String value = map.get(key);
                    dos.writeBytes(PREFIX + BOUNDARY + NEWLINE);
                    dos.writeBytes("Content-Disposition: form-data; "
                            + "name=\"" + key + "\"" + NEWLINE);
                    dos.writeBytes(NEWLINE);
                    dos.writeBytes(URLEncoder.encode(value.toString(), charset));
                    // 或者写成：dos.write(value.toString().getBytes(charset));
                    dos.writeBytes(NEWLINE);
                }
            }

            // 获取表单中上传控件的数据，写入到输出流对象（根据HttpWatch提示的流信息拼凑字符串）；
            if (body_data != null && body_data.length > 0) {
                dos.writeBytes(PREFIX + BOUNDARY + NEWLINE);
                String fileName = filePath.substring(filePath
                        .lastIndexOf(File.separatorChar));
                dos.writeBytes("Content-Disposition: form-data; " + "name=\""
                        + "upload" + "\"" + "; filename=\"" + fileName
                        + "\"" + NEWLINE);
                dos.writeBytes(NEWLINE);
                dos.write(body_data);
                dos.writeBytes(NEWLINE);
            }
            dos.writeBytes(PREFIX + BOUNDARY + PREFIX + NEWLINE);
            dos.flush();

            // 调用HttpURLConnection对象的getResponseCode()获取客户端与服务器端的连接状态码。如果是200，则执行以下操作，否则返回null；
            InputStream in = httpConn.getInputStream();
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                dos.close();
                bis.close();
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return response;
    }
}
