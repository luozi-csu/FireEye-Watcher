package com.luozi.fireeyewatcher.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.WorkPageActivity;
import com.luozi.fireeyewatcher.http.Common;
import com.luozi.fireeyewatcher.model.JWTToken;
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
import java.util.concurrent.TimeUnit;

public class LoginFragment extends Fragment {

    private View view;
    private Context context;
    private EditText et_username;
    private EditText et_pswd;
    private Button btn_login;
    private ProgressBar pb_login;
    private TextView tv_register;
    private CloseableHttpAsyncClient client;
    private static final String LOG_TAG = "LOGIN_FRAGMENT";

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
                    ToastCustom.custom(context, "远程服务器异常");
                    throw new RuntimeException("remote server error");
                } else if (statusCode >= Common.STATUS_REQUEST_ERROR) {
                    ToastCustom.custom(context, "账号或密码错误");
                    throw new RuntimeException("login request error");
                }

                JWTToken jwtToken = JWTToken.parseFromJson(new JSONObject(data));
                Log.d(LOG_TAG, String.format("login successfully, token: %s, desc: %s", jwtToken.jwt, desc));
                Intent intent = new Intent();
                intent.setClass(context, WorkPageActivity.class);
                startActivity(intent);
                ((FragmentActivity)context).finish();
            } catch (RuntimeException | JSONException e) {
                e.printStackTrace();
            } finally {
                pb_login.setVisibility(View.INVISIBLE);
                btn_login.setEnabled(true);
            }
        }

        @Override
        public void failed(Exception e) {
            Log.e(LOG_TAG, String.format("login request failed, error: %s", e.getLocalizedMessage()));
            ToastCustom.custom(context, "连接超时");
            pb_login.setVisibility(View.INVISIBLE);
            btn_login.setEnabled(true);
        }

        @Override
        public void cancelled() {
            Log.d(LOG_TAG, "login request has been cancelled");
            pb_login.setVisibility(View.INVISIBLE);
            btn_login.setEnabled(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        // 异步客户端设置连接超时
        // ConnectionConfig -> ConnectionManager(ConnectionConfig) -> Client(ConnectionManager)
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        et_username = view.findViewById(R.id.et_username);
        et_pswd = view.findViewById(R.id.et_pswd);
        btn_login = view.findViewById(R.id.btn_login);
        pb_login = view.findViewById(R.id.pb_login);
        tv_register = view.findViewById(R.id.tv_register);

        btn_login.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            btn_login.setEnabled(false);

            String username = et_username.getEditableText().toString();
            String password = et_pswd.getEditableText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "账号或密码为空", Toast.LENGTH_SHORT).show();
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
        });

        tv_register.setOnClickListener(view -> {
            FragmentTransaction transaction = ((FragmentActivity)context).getSupportFragmentManager().beginTransaction();
            RegisterFragment registerFragment = new RegisterFragment();
            transaction.replace(R.id.fragment_container_login, registerFragment);
            transaction.commitAllowingStateLoss();
        });

        return view;
    }
}