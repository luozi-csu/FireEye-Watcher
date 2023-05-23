package com.luozi.fireeyewatcher.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.http.Common;
import com.luozi.fireeyewatcher.manager.AppManager;
import com.luozi.fireeyewatcher.utils.Cache;
import com.luozi.fireeyewatcher.utils.ToastCustom;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VideoActivity extends AppCompatActivity {
    private MediaController mediaController;
    private VideoView vv_video;
    private String videoUri;
    private String filePath;
    private ThreadPoolExecutor executor;
    private CloseableHttpClient client;
    private Toolbar toolbar;
    private static final String LOG_TAG = "VIDEO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video);
        client = HttpClients.createDefault();
        executor = new ThreadPoolExecutor(1, 2, 1000, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            AppManager.getInstance().finishActivity();
        });
        vv_video = findViewById(R.id.vv_video);

        Bundle bundle = getIntent().getExtras();
        int recordId = bundle.getInt("record_id");
        videoUri = String.format("http://121.37.255.1:8080/api/v1/videos/%d", recordId);

        filePath = getTempVideo();
        if (filePath == null || filePath.isEmpty()) {
            Log.e(LOG_TAG, "download video failed");
        } else {
            vv_video.setVideoPath(filePath);
            mediaController = new MediaController(this);
            vv_video.setMediaController(mediaController);
            vv_video.requestFocus();
        }
    }

    private String getTempVideo() {
        String tempVideoDir = Cache.getCacheDirectory(this, "cache").getAbsolutePath();
        String path = tempVideoDir + "/" + videoUri.substring(videoUri.lastIndexOf("/") + 1) + ".mp4";
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }

        Future future = executor.submit(() -> {
            HttpGet httpGet = new HttpGet(videoUri);
            httpGet.setHeader("Authorization", Common.access_token);
            httpGet.setHeader("Accept", "*/*");

            boolean success = client.execute(httpGet, response -> {
                if (response.getCode() >= Common.STATUS_SERVER_ERROR) {
                    ToastCustom.custom(this, "远程服务器异常");
                    return false;
                } else if (response.getCode() >= Common.STATUS_UNAUTHORIZED) {
                    ToastCustom.custom(this, "会话已失效，请重新登录");
                    AppManager.getInstance().finishOtherActivity((Activity) this);
                    Intent intent = new Intent();
                    intent.setClass(this, LoginActivity.class);
                    startActivity(intent);
                    AppManager.getInstance().finishActivity((Activity) this);
                } else if (response.getCode() >= Common.STATUS_REQUEST_ERROR) {
                    ToastCustom.custom(this, "请求错误");
                    return false;
                }

                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return false;
                }

                InputStream inputStream = entity.getContent();
                File tempVideo = new File(path);
                FileOutputStream outputStream = new FileOutputStream(tempVideo);
                byte[] buffer = new byte[1024];
                int len = 0;

                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }

                inputStream.close();
                outputStream.close();
                return true;
            });

            return success;
        });

        boolean success = false;
        try {
            success = (boolean) future.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Log.d(LOG_TAG, "video download completely");
        return success ? path : "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
}