package com.luozi.fireeyewatcher.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.http.Common;
import com.luozi.fireeyewatcher.manager.AppManager;
import com.luozi.fireeyewatcher.utils.ToastCustom;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VideoActivity extends AppCompatActivity implements
        MediaController.MediaPlayerControl,
        MediaPlayer.OnBufferingUpdateListener,
        SurfaceHolder.Callback
{
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private SurfaceView sv_video;
    private int bufferPercentage = 0;
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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));
        toolbar.setNavigationOnClickListener(view -> {
            AppManager.getInstance().finishActivity();
        });

        mediaPlayer = new MediaPlayer();
        mediaController = new MediaController(this);
        mediaController.setAnchorView(findViewById(R.id.root_ll));
        initSurfaceView();

        Bundle bundle = getIntent().getExtras();
        int recordId = bundle.getInt("record_id");
        videoUri = String.format("http://121.37.255.1:8080/api/v1/videos/%d", recordId);

        client = HttpClients.createDefault();
        executor = new ThreadPoolExecutor(1, 2, 1000, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    private void initSurfaceView() {
        sv_video = findViewById(R.id.sv_video);
        sv_video.setZOrderOnTop(false);
        sv_video.getHolder().addCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            filePath = getTempVideo();

            if (filePath == null || filePath.isEmpty()) {
                Log.e(LOG_TAG, "视频下载失败");
                mediaController.setMediaPlayer(this);
                mediaController.setEnabled(false);
            } else {
                Log.e(LOG_TAG, "视频下载完成");
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.setOnBufferingUpdateListener(this);

                mediaController.setMediaPlayer(this);
                mediaController.setEnabled(true);
            }
        } catch (IOException e){
            Log.e(LOG_TAG, String.format("load video(uri=%s) failed", videoUri));
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mediaPlayer){
            mediaPlayer.release();
            mediaPlayer = null;
        }
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        Log.d(LOG_TAG, "on destroy");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mediaController.show();
        return super.onTouchEvent(event);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        bufferPercentage = percent;
    }

    // SurfaceHolder.Callback
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mediaPlayer.setDisplay(holder);
        mediaPlayer.prepareAsync();
    }

    // SurfaceHolder.Callback
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    // SurfaceHolder.Callback
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return bufferPercentage;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private String getTempVideo() {
        String tempVideoDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath();
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
                Files.copy(inputStream, Paths.get(path));
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
}