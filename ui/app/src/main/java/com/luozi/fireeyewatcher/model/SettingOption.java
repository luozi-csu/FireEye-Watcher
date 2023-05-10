package com.luozi.fireeyewatcher.model;

import com.luozi.fireeyewatcher.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingOption {
    public int drawable;
    public String text;

    public SettingOption(int drawable, String text) {
        this.drawable = drawable;
        this.text = text;
    }

    public static List<SettingOption> getInstance() {
        SettingOption[] options = new SettingOption[] {
                new SettingOption(
                        R.mipmap.update,
                        "修改密码"
                ),
                new SettingOption(
                        R.mipmap.statistic,
                        "统计信息"
                ),
                new SettingOption(
                        R.mipmap.setting,
                        "设置"
                ),
                new SettingOption(
                        R.mipmap.help,
                        "帮助"
                ),
                new SettingOption(
                        R.mipmap.about,
                        "关于"
                )
        };
        return Arrays.asList(options);
    }
}
