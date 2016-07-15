package com.kenos.kenos.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;

/**
 * User: hxk(huangxikang@520dyw.cn)
 * Date: 2016-07-15
 * Time: 15:26
 * Description:
 */
public class KenApplication extends Application {
    public static Context context;
    private static KenApplication instance;
    // login user name
    public final String PREF_USERNAME = "username";

    /**
     * nickname for current user, the nickname instead of ID be shown when user receive notification from APNs
     */
    public static String currentUserNick = "";

    @Override
    public void onCreate() {
        MultiDex.install(this);
        super.onCreate();
        context = this;
        instance = this;
        EMOptions options = null;
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        KenOsManager.getInstance().onInit(context, options);
    }

    public static KenApplication getInstance() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
