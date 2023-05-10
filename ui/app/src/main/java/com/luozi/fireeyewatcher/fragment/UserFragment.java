package com.luozi.fireeyewatcher.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.luozi.fireeyewatcher.activity.AboutActivity;
import com.luozi.fireeyewatcher.activity.LoginActivity;
import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.activity.UpdateUserActivity;
import com.luozi.fireeyewatcher.adapter.SettingListAdapter;
import com.luozi.fireeyewatcher.http.Common;
import com.luozi.fireeyewatcher.manager.AppManager;
import com.luozi.fireeyewatcher.model.SettingOption;
import com.luozi.fireeyewatcher.utils.ToastCustom;
import com.luozi.fireeyewatcher.view.CornerListView;

import java.util.Arrays;
import java.util.List;

public class UserFragment extends Fragment {
    private View view;
    private Context context;
    private TextView tv_username;
    private CornerListView setting_list;
    private TextView tv_logout;
    private static final String[] options = new String[]{"修改密码", "统计信息", "设置", "关于"};
    private static final String LOG_TAG = "USER_FRAGMENT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user, container, false);
        tv_username = view.findViewById(R.id.tv_username);
        setting_list = view.findViewById(R.id.setting_list);
        tv_logout = view.findViewById(R.id.tv_logout);

        tv_username.setText(String.format("当前账户：%s", Common.loginUser.username));

        SettingListAdapter settingListAdapter = new SettingListAdapter(context, SettingOption.getInstance());
        setting_list.setAdapter(settingListAdapter);
        setting_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                switch(i) {
                    case 0:
                        intent.setClass(context, UpdateUserActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        // todo: use server statistic api
                        break;
                    case 2:
                        // todo: setting
                        break;
                    case 3:
                        // todo: help
                        break;
                    case 4:
                        intent.setClass(context, AboutActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });

        tv_logout.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("确定退出登录？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AppManager.getInstance().finishOtherActivity((Activity)context);
                            Intent intent = new Intent();
                            intent.setClass(context, LoginActivity.class);
                            startActivity(intent);
                            AppManager.getInstance().finishActivity((Activity)context);
                        }
                    })
                    .setNegativeButton("取消", (dialogInterface, i) -> {
                        // do nothing
                    });
            builder.create().show();
        });

        return view;
    }
}