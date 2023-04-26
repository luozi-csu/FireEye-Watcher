package com.luozi.fireeyewatcher.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.luozi.fireeyewatcher.LoginActivity;
import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.adapter.RecordAdapter;
import com.luozi.fireeyewatcher.http.Common;
import com.luozi.fireeyewatcher.manager.AppManager;
import com.luozi.fireeyewatcher.model.Record;
import com.luozi.fireeyewatcher.utils.ToastCustom;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.message.StatusLine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RecordFragment extends Fragment {
    private SwipeRefreshLayout swipe_layout;
    private ListView lv_record;
    private Context context;
    private View view;
    private ThreadPoolExecutor executor;
    private CloseableHttpClient client;
    private List<Record> recordList;
    private static final String LOG_TAG = "RECORD_FRAGMENT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        client = HttpClients.createDefault();
        executor = new ThreadPoolExecutor(1, 2, 1000, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    private void updateListView() {
        Future future =  executor.submit(() -> {
            getRecords();
            return null;
        });
        try {
            future.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        RecordAdapter adapter = new RecordAdapter(context, recordList);
        lv_record.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_record, container, false);
        swipe_layout = view.findViewById(R.id.swipe_layout);
        lv_record = view.findViewById(R.id.lv_record);

        updateListView();

        swipe_layout.setColorSchemeColors(context.getResources().getColor(R.color.theme, null));
        swipe_layout.setOnRefreshListener(() -> {
            recordList.clear();
            updateListView();
            new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    swipe_layout.setRefreshing(false);
                }
            }.sendEmptyMessage(0);
        });

        return view;
    }

    private void getRecords() {
        if (recordList == null) {
            recordList = new ArrayList<>();
        }

        HttpGet httpGet = new HttpGet(String.format("http://121.37.255.1:8080/api/v1/records?uid=%d", Common.loginUser.id));
        httpGet.setHeader("Authorization", Common.access_token);
        httpGet.setHeader("Accept", "*/*");

        String res = null;
        try {
            res = client.execute(httpGet, response -> {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return null;
                }

                InputStream inputStream = entity.getContent();
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String read;

                while ((read = bufferedReader.readLine()) != null) {
                    stringBuilder.append(read);
                }

                return stringBuilder.toString();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (res == null || res.length() == 0) {
            Log.d(LOG_TAG, "empty response");
        }

        try {
            JSONObject jsonResponse = new JSONObject(res);

            int statusCode = jsonResponse.getInt("status_code");
            String desc = jsonResponse.getString("desc");
            JSONArray records = jsonResponse.getJSONArray("data");

            if (statusCode >= Common.STATUS_SERVER_ERROR) {
                ToastCustom.custom(context, "远程服务器异常");
                throw new RuntimeException("remote server error");
            } else if (statusCode >= Common.STATUS_UNAUTHORIZED) {
                ToastCustom.custom(context, "会话已失效，请重新登录");
                AppManager.getInstance().finishOtherActivity((Activity) context);
                Intent intent = new Intent();
                intent.setClass(context, LoginActivity.class);
                startActivity(intent);
                AppManager.getInstance().finishActivity((Activity) context);
            } else if (statusCode >= Common.STATUS_REQUEST_ERROR) {
                ToastCustom.custom(context, "请求错误");
                throw new RuntimeException("get records request error");
            }

            for (int i = 0; i < records.length(); i++) {
                Record record = Record.parseFromJson(records.getJSONObject(i));
                recordList.add(record);
            }

            Log.d(LOG_TAG, recordList.toString());
        } catch (RuntimeException | JSONException e) {
            e.printStackTrace();
        }
    }
}