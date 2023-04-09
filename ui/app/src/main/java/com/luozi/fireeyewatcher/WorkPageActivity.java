package com.luozi.fireeyewatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.luozi.fireeyewatcher.fragment.CameraFragment;

public class WorkPageActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_title;
    private TextView tv_camera;
    private TextView tv_me;
    private CameraFragment cameraFragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_work_page);

        tv_title = findViewById(R.id.tv_title);
        tv_camera = findViewById(R.id.tv_camera);
        tv_me = findViewById(R.id.tv_me);

        tv_camera.setOnClickListener(this);
        tv_me.setOnClickListener(this);

        fragmentManager = getSupportFragmentManager();

        tv_camera.performClick();
    }

    private void setSelected() {
        tv_camera.setSelected(false);
        tv_me.setSelected(false);
    }

    private void hideAllFragment(FragmentTransaction transaction) {
        if (cameraFragment != null) transaction.hide(cameraFragment);
    }

    @Override
    public void onClick(View view) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideAllFragment(transaction);
        switch (view.getId()) {
            case R.id.tv_camera:
                setSelected();
                tv_camera.setSelected(true);
                tv_title.setText("相机");
                if (cameraFragment == null) {
                    cameraFragment = new CameraFragment();
                    transaction.add(R.id.fragment_content, cameraFragment);
                }
                transaction.show(cameraFragment);
                break;
        }
        transaction.commitAllowingStateLoss();
    }
}