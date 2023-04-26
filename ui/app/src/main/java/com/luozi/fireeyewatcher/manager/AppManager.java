package com.luozi.fireeyewatcher.manager;

import android.app.Activity;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Stack;

public class AppManager {

    private Stack<WeakReference<Activity>> mActivityStack;
    private static volatile AppManager mInstance;

    private AppManager() {

    }

    public static AppManager getInstance() {
        if (mInstance == null) {
            synchronized (AppManager.class) {
                if (mInstance == null) {
                    mInstance = new AppManager();
                }
            }
        }
        return mInstance;

    }

    /**
     * 添加Activity到堆栈
     * @param activity activity实例
     */
    public void addActivity(Activity activity) {
        if (mActivityStack == null) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.add(new WeakReference<>(activity));
    }

    /**
     * 检查弱引用是否释放，若释放，则从栈中清理掉该元素
     */
    public void checkWeakReference() {
        if (mActivityStack != null) {
            for (Iterator<WeakReference<Activity>> it = mActivityStack.iterator(); it.hasNext(); ) {
                WeakReference<Activity> activityReference = it.next();
                Activity temp = activityReference.get();
                if (temp == null) {
                    it.remove();// 使用迭代器来进行安全的加锁操作
                }
            }
        }
    }

    /**
     * 获取当前Activity
     * @return 当前（栈顶）activity
     */
    public Activity currentActivity() {
        checkWeakReference();
        if (mActivityStack != null && !mActivityStack.isEmpty()) {
            return mActivityStack.lastElement().get();
        }
        return null;
    }

    /**
     * 结束除当前activtiy以外的所有activity
     * @param activtiy 不需要结束的activity
     */
    public void finishOtherActivity(Activity activtiy) {
        if (mActivityStack != null && activtiy != null) {
            for (Iterator<WeakReference<Activity>> it = mActivityStack.iterator(); it.hasNext(); ) {
                WeakReference<Activity> activityReference = it.next();
                Activity temp = activityReference.get();
                if (temp == null) {
                    // 清理掉已经释放的activity
                    it.remove();
                    continue;
                }
                if (temp != activtiy) {
                    // 使用迭代器来进行安全的加锁操作
                    it.remove();
                    temp.finish();
                }
            }
        }
    }

    /**
     * 结束除指定activtiy 集合以外的所有activity
     * @param cls 指定的某类activity
     */
    public void finishOtherActivity(Class<?> cls) {
        if (mActivityStack != null) {
            for (Iterator<WeakReference<Activity>> it = mActivityStack.iterator(); it.hasNext(); ) {
                WeakReference<Activity> activityReference = it.next();
                Activity activity = activityReference.get();
                if (activity == null) {
                    // 清理掉已经释放的activity
                    it.remove();
                    continue;
                }
                if (!activity.getClass().equals(cls)) {
                    // 使用迭代器来进行安全的加锁操作
                    it.remove();
                    activity.finish();
                }
            }
        }
    }

    /**
     * 结束当前Activity
     */
    public void finishActivity() {
        Activity activity = currentActivity();
        if (activity != null) {
            finishActivity(activity);
        }
    }

    /**
     * 结束指定的Activity
     * @param activity 指定的activity实例
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            for (Iterator<WeakReference<Activity>> it = mActivityStack.iterator(); it.hasNext(); ) {
                WeakReference<Activity> activityReference = it.next();
                Activity temp = activityReference.get();
                if (temp == null) {
                    // 清理掉已经释放的activity
                    it.remove();
                    continue;
                }
                if (temp == activity) {
                    it.remove();
                }
            }
            activity.finish();
        }
    }

    /**
     * 结束指定类名的所有Activity
     * @param cls 指定的类的class
     */
    public void finishActivity(Class<?> cls) {
        if (mActivityStack != null) {
            for (Iterator<WeakReference<Activity>> it = mActivityStack.iterator(); it.hasNext(); ) {
                WeakReference<Activity> activityReference = it.next();
                Activity activity = activityReference.get();
                if (activity == null) {
                    // 清理掉已经释放的activity
                    it.remove();
                    continue;
                }
                if (activity.getClass().equals(cls)) {
                    it.remove();
                    activity.finish();
                }
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        if (mActivityStack != null) {
            for (Iterator<WeakReference<Activity>> it = mActivityStack.iterator(); it.hasNext(); ) {
                WeakReference<Activity> activityReference = it.next();
                Activity activity = activityReference.get();
                if (activity != null) {
                    activity.finish();
                }
            }
            mActivityStack.clear();
        }
    }

    /**
     * 退出应用程序
     */
    public void exitApp() {
        try {
            finishAllActivity();
            // 从操作系统中结束掉当前程序的进程
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            Log.e("Exit exception", e.getMessage());
        }

    }
}