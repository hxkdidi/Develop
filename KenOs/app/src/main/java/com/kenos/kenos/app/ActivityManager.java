package com.kenos.kenos.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.kenos.kenos.R;
import com.kenos.kenos.activity.ShowBigImageActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActivityManager {
    private final String TAG = ActivityManager.this.getClass().getSimpleName();

    private List<Activity> activityList = new ArrayList<>();
    private static ActivityManager instance;

    private ActivityManager() {
    }

    // 单例模式中获取唯一的MyApplication实例  
    public static ActivityManager getInstance() {
        if (null == instance) {
            instance = new ActivityManager();
        }
        return instance;
    }

    /**
     * 添加activity 到列表
     */
    public void addActivity(Activity activity) {
        if (null != activity) {
            activityList.add(activity);
        }
    }


    /**
     * 从列表中删除activity
     */
    public void removeActivity(Activity activity) {
        if (null != activity) {
            activityList.remove(activity);
        }
    }

    /**
     * 关闭所有的activity
     */
    public void finishAllActivity() {
        for (Activity activity : activityList) {
            if (null != activity && activity.isFinishing() == false) {
                if (null != activity) {
                    Log.d(TAG, "finish activity " + activity.getClass().getSimpleName());
                    activity.finish();
                }
            }
        }
    }

    /**
     * 退出程序并关闭所有的activity
     */
    public void exit() {
        finishAllActivity();
    }

    /*结束界面*/
    public void finishAllActivityExcept(Class<? extends Activity> aClass) {
        for (Activity activity : activityList) {
            if (null != activity && activity.isFinishing() == false) {
                if (!aClass.equals(activity.getClass())) {
                    Log.d(TAG, "finish activity " + activity.getClass().getSimpleName());
                    activity.finish();
                }
            }
        }
    }

    /**
     * 查看大图
     *
     * @param mContext
     */
    public void showBigImage(Activity mContext, EMMessage message) {
        Intent intent = new Intent(mContext, ShowBigImageActivity.class);
        EMImageMessageBody body = (EMImageMessageBody) message.getBody();
        File file = new File(body.getLocalUrl());
        if (file.exists()) {
            Uri uri = Uri.fromFile(file);
            intent.putExtra("uri", uri);
        } else {
            // The local full size pic does not exist yet.
            // ShowBigImage needs to download it from the server
            // first
            intent.putExtra("secret", body.getSecret());
            intent.putExtra("remotepath", body.getRemoteUrl());
            intent.putExtra("localUrl", body.getLocalUrl());
        }
        mContext.startActivity(intent);
        mContext.overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
    }
}
