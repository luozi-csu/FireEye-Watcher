package com.luozi.fireeyewatcher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.luozi.fireeyewatcher.http.Common;
import com.luozi.fireeyewatcher.http.HttpClient;
import com.luozi.fireeyewatcher.http.HttpRequest;
import com.luozi.fireeyewatcher.http.HttpRequestTask;
import com.luozi.fireeyewatcher.http.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private HttpClient client;
    private EditText et_username;
    private EditText et_pswd;
    private static final String LOGIN_TAG = "LOGIN";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        client = new HttpClient();

        et_username = findViewById(R.id.et_username);
        et_pswd = findViewById(R.id.et_pswd);
        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
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
            data.put("user_name", username);
            data.put("password", password);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        HttpRequest request = new HttpRequest("POST", "http://10.0.2.2:8080/login", null, null, data);
        HttpRequestTask task = new HttpRequestTask(client, request);
        HttpResponse response = task.execute();

        if (response == null) {
            Log.w(LOGIN_TAG, String.format("连接超时: [%s] %s", request.method, request.url));
            Toast.makeText(this, "连接超时", Toast.LENGTH_SHORT).show();
        } else if (response.statusCode >= Common.STATUS_SERVER_ERROR) {
            Log.w(LOGIN_TAG, String.format("远程服务器错误: [%s] %s", request.method, request.url));
            Toast.makeText(this, "远程服务器错误", Toast.LENGTH_SHORT).show();
        } else if (response.statusCode >= Common.STATUS_REQUEST_ERROR) {
            Log.e(LOGIN_TAG, String.format("请求错误: [%s] %s", request.method, request.url));
            Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent();
            intent.setClass(this, WelcomeActivity.class);
            startActivity(intent, null);
        }
    }
}