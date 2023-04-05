package com.luozi.fireeyewatcher;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.luozi.fireeyewatcher.http.HttpClient;
import com.luozi.fireeyewatcher.model.HttpResponse;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        HttpClient client = new HttpClient("http", "10.0.2.2", 8080);
        HttpResponse response = client.doRequest("GET", "hello", null, null, null);
        Log.d("HTTP_REQUEST_DONE", String.format("got response: %s", response));
    }
}