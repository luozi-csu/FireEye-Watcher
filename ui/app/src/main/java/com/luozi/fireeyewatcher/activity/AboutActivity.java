package com.luozi.fireeyewatcher.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Window;

import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.manager.AppManager;


public class AboutActivity extends AppCompatActivity {
    private Toolbar toolbar_about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_about);

        toolbar_about = findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar_about);
        toolbar_about.setNavigationOnClickListener(view -> {
            AppManager.getInstance().finishActivity();
        });
    }
}