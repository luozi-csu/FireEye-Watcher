package com.luozi.fireeyewatcher.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.manager.AppManager;

import java.io.IOException;

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
}