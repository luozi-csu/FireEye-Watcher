package com.luozi.fireeyewatcher.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.fragment.LoginFragment;
import com.luozi.fireeyewatcher.fragment.RegisterFragment;
import com.luozi.fireeyewatcher.manager.AppManager;

public class LoginActivity extends AppCompatActivity {

    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        loginFragment = new LoginFragment();
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fragment_container_login, loginFragment);
        transaction.show(loginFragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (loginFragment != null) transaction.remove(loginFragment);
        if (registerFragment != null) transaction.remove(registerFragment);
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