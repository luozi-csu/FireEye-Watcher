package com.luozi.fireeyewatcher.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.fragment.CameraFragment;
import com.luozi.fireeyewatcher.fragment.RecordFragment;
import com.luozi.fireeyewatcher.fragment.UserFragment;
import com.luozi.fireeyewatcher.manager.AppManager;

public class WorkPageActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_title;
    private TextView tv_camera;
    private TextView tv_record;
    private TextView tv_me;
    private CameraFragment cameraFragment;
    private RecordFragment recordFragment;
    private UserFragment userFragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_work_page);

        tv_title = findViewById(R.id.tv_title);
        tv_camera = findViewById(R.id.tv_camera);
        tv_record = findViewById(R.id.tv_record);
        tv_me = findViewById(R.id.tv_me);

        tv_camera.setOnClickListener(this);
        tv_record.setOnClickListener(this);
        tv_me.setOnClickListener(this);

        fragmentManager = getSupportFragmentManager();

        tv_camera.performClick();
    }

    private void setSelected() {
        tv_camera.setSelected(false);
        tv_record.setSelected(false);
        tv_me.setSelected(false);
    }

    private void hideAllFragment(FragmentTransaction transaction) {
        if (cameraFragment != null) transaction.hide(cameraFragment);
        if (recordFragment != null) transaction.hide(recordFragment);
        if (userFragment != null) transaction.hide(userFragment);
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
            case R.id.tv_record:
                setSelected();;
                tv_record.setSelected(true);
                tv_title.setText("记录");
                if (recordFragment == null) {
                    recordFragment = new RecordFragment();
                    transaction.add(R.id.fragment_content, recordFragment);
                }
                transaction.show(recordFragment);
                break;
            case R.id.tv_me:
                setSelected();
                tv_me.setSelected(true);
                tv_title.setText("我");
                if (userFragment == null) {
                    userFragment = new UserFragment();
                    transaction.add(R.id.fragment_content, userFragment);
                }
                transaction.show(userFragment);
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (cameraFragment != null) transaction.remove(cameraFragment);
        if (recordFragment != null) transaction.remove(recordFragment);
        if (userFragment != null) transaction.remove(userFragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}