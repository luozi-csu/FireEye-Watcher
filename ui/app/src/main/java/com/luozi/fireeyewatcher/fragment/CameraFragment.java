package com.luozi.fireeyewatcher.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.utils.DateUtil;
import com.luozi.fireeyewatcher.utils.ToastCustom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CameraFragment extends Fragment {

    private Context context;
    private View view;
    private Button btn_video;
    private PreviewView viewFinder;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private static List<String> REQUIRED_PERMISSIONS;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String PREFIX = "FireEye_";
    private static final String LOG_TAG = "CAMERA_FRAGMENT";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        REQUIRED_PERMISSIONS = new ArrayList<>();
        REQUIRED_PERMISSIONS.add(Manifest.permission.CAMERA);
        REQUIRED_PERMISSIONS.add(Manifest.permission.RECORD_AUDIO);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            REQUIRED_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_camera, container, false);
        btn_video = view.findViewById(R.id.btn_video);
        viewFinder = view.findViewById(R.id.viewFinder);

        if (allPermissionGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    REQUIRED_PERMISSIONS.toArray(new String[REQUIRED_PERMISSIONS.size()]),
                    REQUEST_CODE_PERMISSIONS
            );
        }

        btn_video.setOnClickListener(view -> {
            if (videoCapture == null) {
                return;
            }

            btn_video.setEnabled(false);

            Recording curRecording = recording;
            if (curRecording != null) {
                curRecording.stop();
                recording = null;
                return;
            }

            String name = PREFIX + DateUtil.timeNow();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Movies/FireEye-Watcher");
            }

            MediaStoreOutputOptions outputOptions = new MediaStoreOutputOptions.Builder(
                    context.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            ).setContentValues(contentValues).build();

            PendingRecording pendingRecording = videoCapture.getOutput()
                    .prepareRecording(context, outputOptions);

            if (PermissionChecker.checkSelfPermission(context,
                    Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_GRANTED) {
                pendingRecording.withAudioEnabled();
            }

            recording = pendingRecording.start(
                    ContextCompat.getMainExecutor(context),
                    videoRecordEvent -> {
                        if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                            btn_video.setBackground(context.getDrawable(R.drawable.capture_stop_shape));
                            btn_video.setEnabled(true);
                        }
                        else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                            if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                                ToastCustom.custom(context, "拍摄成功");
                            }
                            else {
                                if (recording != null) {
                                    recording.close();
                                }
                                recording = null;

                                Log.e(LOG_TAG, String.format("video capture failed, error: %s", ((VideoRecordEvent.Finalize) videoRecordEvent).getError()));
                            }
                            btn_video.setBackground(context.getDrawable(R.drawable.capture_ready_shape));
                            btn_video.setEnabled(true);
                        }
                    }
            );
        });

        return view;
    }

    private boolean allPermissionGranted() {
        boolean granted = true;
        for (String permission: REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                Log.e(LOG_TAG, String.format("permission denied: %s", permission));
                granted = false;
            }
        }
        return granted;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        (LifecycleOwner) context, cameraSelector, preview, videoCapture
                );

            } catch (ExecutionException | InterruptedException e) {
                Log.e(LOG_TAG, String.format("get camera provider failed, error: %s", e.getLocalizedMessage()));
                throw new RuntimeException(e);
            }

        }, ContextCompat.getMainExecutor(context));
    }
}
