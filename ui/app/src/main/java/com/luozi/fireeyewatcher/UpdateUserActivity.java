package com.luozi.fireeyewatcher;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.luozi.fireeyewatcher.http.Common;
import com.luozi.fireeyewatcher.manager.AppManager;
import com.luozi.fireeyewatcher.model.User;
import com.luozi.fireeyewatcher.utils.ToastCustom;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Method;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UpdateUserActivity extends AppCompatActivity {

    private EditText et_new_pswd;
    private EditText et_new_pswd_check;
    private Button btn_confirm;
    private ProgressBar pb_update;
    private CloseableHttpAsyncClient client;
    private static final String LOG_TAG = "UPDATE_USER";

    private class UpdateUserRequestCallback implements FutureCallback<SimpleHttpResponse> {

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
                    ToastCustom.custom(UpdateUserActivity.this, "远程服务器异常");
                    throw new RuntimeException("remote server error");
                } else if (statusCode >= Common.STATUS_UNAUTHORIZED) {
                    ToastCustom.custom(UpdateUserActivity.this, "会话已失效，请重新登录");
                    AppManager.getInstance().finishOtherActivity((Activity) UpdateUserActivity.this);
                    Intent intent = new Intent();
                    intent.setClass(UpdateUserActivity.this, LoginActivity.class);
                    startActivity(intent);
                    AppManager.getInstance().finishActivity((Activity) UpdateUserActivity.this);
                } else if (statusCode >= Common.STATUS_REQUEST_ERROR) {
                    ToastCustom.custom(UpdateUserActivity.this, String.format("请求错误: %s", desc));
                    throw new RuntimeException("register request error");
                }

                User newUser = User.parseFromJson(new JSONObject(data));
                Log.d(LOG_TAG, String.format("update successfully, uid: %d, name: %s", newUser.id, newUser.username));
                ToastCustom.custom(UpdateUserActivity.this, "修改成功，请重新登录");
                Intent intent = new Intent();
                intent.setClass(UpdateUserActivity.this, LoginActivity.class);
                startActivity(intent);
                AppManager.getInstance().finishActivity(UpdateUserActivity.this);
            } catch (RuntimeException | JSONException e) {
                e.printStackTrace();
            } finally {
                pb_update.setVisibility(View.INVISIBLE);
                btn_confirm.setEnabled(true);
            }
        }

        @Override
        public void failed(Exception e) {
            Log.e(LOG_TAG, String.format("update user request failed, error: %s", e.getLocalizedMessage()));
            ToastCustom.custom(UpdateUserActivity.this, "连接超时");
            pb_update.setVisibility(View.INVISIBLE);
            btn_confirm.setEnabled(true);
        }

        @Override
        public void cancelled() {
            Log.d(LOG_TAG, "login request has been cancelled");
            pb_update.setVisibility(View.INVISIBLE);
            btn_confirm.setEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_update_user);

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(5, TimeUnit.SECONDS)
                .setSocketTimeout(5, TimeUnit.SECONDS)
                .build();
        PoolingAsyncClientConnectionManager poolingAsyncClientConnectionManager =
                PoolingAsyncClientConnectionManagerBuilder.create()
                        .setDefaultConnectionConfig(connectionConfig)
                        .build();
        client = HttpAsyncClients.custom()
                .setConnectionManager(poolingAsyncClientConnectionManager)
                .build();

        et_new_pswd = findViewById(R.id.et_new_pswd);
        et_new_pswd_check = findViewById(R.id.et_new_pswd_check);
        btn_confirm = findViewById(R.id.btn_confirm);
        pb_update = findViewById(R.id.pb_update);

        btn_confirm.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            btn_confirm.setEnabled(false);

            String newPassword = et_new_pswd.getEditableText().toString();
            String newPasswordCheck = et_new_pswd_check.getEditableText().toString();

            if (newPassword.compareTo(newPasswordCheck) != 0) {
                ToastCustom.custom(this, "两次输入密码不一致");
                btn_confirm.setEnabled(true);
                return;
            }

            JSONObject data = new JSONObject();
            Map<String, Object> newUser = new HashMap<>();
            newUser.put("id", Common.loginUser.id);
            newUser.put("name", Common.loginUser.username);
            newUser.put("password", newPassword);
            try {
                data.put("new_user", new JSONObject(newUser));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            URI uri = null;
            try {
                uri = new URI(String.format("http://121.37.255.1:8080/api/v1/users?name=%s", Common.loginUser.username));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            SimpleHttpRequest request = new SimpleHttpRequest(Method.PUT, uri);
            request.setHeader("Authorization", Common.access_token);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setBody(data.toString(), ContentType.APPLICATION_JSON);

            pb_update.setVisibility(View.VISIBLE);
            client.start();
            client.execute(request, new UpdateUserRequestCallback());
        });
    }
}