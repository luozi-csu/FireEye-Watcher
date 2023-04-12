package com.luozi.fireeyewatcher.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastCustom {

    private static Handler handler = new Handler(Looper.getMainLooper());
    private static Toast mToast; //避免Toast多次频繁弹出
    private static Context mContext;

    public static void custom(Context context, String text) {
        if (context == null)
            return;
        mContext = context;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) {
                    mToast = new Toast(mContext);
                }
                mToast.setText(text);
                mToast.setDuration(Toast.LENGTH_SHORT);
                mToast.show();
            }
        });
    }
}
