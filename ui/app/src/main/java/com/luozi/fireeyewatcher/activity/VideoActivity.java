package com.luozi.fireeyewatcher.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.MediaController;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.http.Common;
import com.luozi.fireeyewatcher.manager.AppManager;
import com.luozi.fireeyewatcher.utils.ToastCustom;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private ProgressBar pb_video;
    private int bufferPercentage = 0;
    private String videoUri;
    private ThreadPoolExecutor executor;
    private CloseableHttpClient client;
    private static final String LOG_TAG = "VIDEO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video);

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


            mediaPlayer.setDataSource(videoUri);
            mediaPlayer.setOnBufferingUpdateListener(this);

            mediaController.setMediaPlayer(this);
            mediaController.setEnabled(true);
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

    private void getTempVideo() {
        Future future = executor.submit(() -> {
            HttpGet httpGet = new HttpGet(videoUri);
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
            } catch (RuntimeException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        });
    }
}