package com.luozi.fireeyewatcher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.luozi.fireeyewatcher.http.Common;
import com.luozi.fireeyewatcher.model.JWTToken;
import com.luozi.fireeyewatcher.utils.ToastCustom;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Method;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private CloseableHttpAsyncClient client;
    private EditText et_username;
    private EditText et_pswd;
    private ProgressBar pb_login;
    private static final String LOG_TAG = "LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        client = HttpAsyncClients.custom().build();
        et_username = findViewById(R.id.et_username);
        et_pswd = findViewById(R.id.et_pswd);
        pb_login = findViewById(R.id.pb_login);
        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
    }

    private class LoginRequestCallback implements FutureCallback<SimpleHttpResponse> {

        @Override
        public void completed(SimpleHttpResponse httpResponse) {
            try {
                if (httpResponse == null) {
                    throw new RuntimeException("empty http response");
                }

                JSONObject jsonResponse = new JSONObject(httpResponse.getBodyText());

                int statusCode = jsonResponse.getInt("status_code");
                String desc = jsonResponse.getString("desc");
                String data = jsonResponse.getString("data");

                if (statusCode >= Common.STATUS_SERVER_ERROR) {
                    ToastCustom.custom(LoginActivity.this, "远程服务器异常");
                    throw new RuntimeException("remote server error");
                } else if (statusCode >= Common.STATUS_REQUEST_ERROR) {
                    ToastCustom.custom(LoginActivity.this, "账号或密码错误");
                    throw new RuntimeException("login request error");
                }

                JWTToken jwtToken = JWTToken.parseFromJson(new JSONObject(data));
                Log.d(LOG_TAG, String.format("login success, token: %s, desc: %s", jwtToken.jwt, desc));
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, WorkPageActivity.class);
                startActivity(intent);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } finally {
                pb_login.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void failed(Exception e) {
            Log.e(LOG_TAG, String.format("login request failed, error: %s", e.getLocalizedMessage()));
            ToastCustom.custom(LoginActivity.this, "连接超时");
            pb_login.setVisibility(View.INVISIBLE);
        }

        @Override
        public void cancelled() {
            Log.d(LOG_TAG, "login request has been cancelled");
            pb_login.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        String username = et_username.getEditableText().toString();
        String password = et_pswd.getEditableText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "账号或密码为空", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject data = new JSONObject();
        try {
            data.put("name", username);
            data.put("password", password);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        URI uri = null;
        try {
            uri = new URI("http://10.0.2.2:8080/api/v1/auth/login");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        SimpleHttpRequest request = new SimpleHttpRequest(Method.POST, uri);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");
        request.setBody(data.toString(), ContentType.APPLICATION_JSON);

        pb_login.setVisibility(View.VISIBLE);
        client.start();
        client.execute(request, new LoginRequestCallback());
    }
}