package com.luozi.fireeyewatcher.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.luozi.fireeyewatcher.BuildConfig;
import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.http.HttpClient;
import com.luozi.fireeyewatcher.http.HttpRequest;
import com.luozi.fireeyewatcher.http.HttpResponse;
import com.luozi.fireeyewatcher.utils.DateUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CameraFragment extends Fragment implements View.OnClickListener {
    private View view;
    private Context context;
    private TextView tv_camera_big;
    private static final String LOG_TAG = "CAMERA_FRAGMENT";
    private ActivityResultLauncher launcher;

    private static class HttpResponseBuffer {
        private HttpResponse response;
        private boolean valueSet = false;

        public synchronized void put(HttpResponse response) {
            if (valueSet) {
                try {
                    wait(1000000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            this.response = response;
            valueSet = true;
            notify();
        }

        public synchronized HttpResponse get() {
            if (!valueSet) {
                try {
                    wait(1000000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            valueSet = false;
            notify();
            return this.response;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Toast.makeText(context, "拍摄完成", Toast.LENGTH_SHORT).show();
//                Intent intent = result.getData();
//                Bundle bundle = intent.getExtras();
//                String tmp = bundle.getString("dat");
//                Toast.makeText(context, tmp, Toast.LENGTH_SHORT).show();
//                HttpResponseBuffer responseBuffer = new HttpResponseBuffer();
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String absPath = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/" + videoname;
//
//                        BufferedInputStream bis = null;
//                        byte[] body_data = null;
//                        try {
//                            bis = new BufferedInputStream(new FileInputStream(absPath));
//                        } catch (FileNotFoundException e) {
//                            throw new RuntimeException(e);
//                        }
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        int c = 0;
//                        byte[] buffer = new byte[8 * 1024];
//                        try {
//                            while ((c = bis.read(buffer)) != -1) {
//                                baos.write(buffer, 0, c);
//                                baos.flush();
//                            }
//                            body_data = baos.toByteArray();
//                            baos.close();;
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                        HttpClient client = new HttpClient();
//                        HttpResponse response = client.doFileUpload("http://10.0.2.2:8080/api/v1/upload", null, videoname, body_data, "utf-8");
//                        responseBuffer.put(response);
//                    }
//                }).start();
//
//                HttpResponse response = responseBuffer.get();
//                Log.d(LOG_TAG, response.toString());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_camera, container, false);
        tv_camera_big = view.findViewById(R.id.tv_camera_big);
        tv_camera_big.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        String videoname = "FireEye_" + DateUtil.timeNow() + ".mp4";
        File videoFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath(), videoname);
//        Log.d(LOG_TAG, context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath());
        try {
            if (videoFile.exists()) {
                videoFile.delete();
            }
            videoFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Uri videoUri;
        if (Build.VERSION.SDK_INT >= 24) {
            videoUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, videoFile);
        } else {
            videoUri = Uri.fromFile(videoFile);
        }

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
        launcher.launch(intent);
    }
}
