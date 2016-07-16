package com.kenos.kenos.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.kenos.kenos.Constant;
import com.kenos.kenos.chat.KenOsManager;
import com.kenos.kenos.db.EaseUser;
import com.kenos.kenos.db.UserInfo;
import com.kenos.kenos.db.UserDao;

import java.util.ArrayList;
import java.util.Map;

/**
 * User: hxk(huangxikang@520dyw.cn)
 * Date: 2016-07-15
 * Time: 15:26
 * Description:
 */
public class KenApplication extends MultiDexApplication {
    public static Context mContext;
    private static KenApplication instance;
    private String username = "";
    private Map<String, EaseUser> contactList;
    private UserDao userDao;


    @Override
    public void onCreate() {
        MultiDex.install(this);
        super.onCreate();
        mContext = this;
        instance = this;
        // 初始化数据库
        if (KenOsManager.getInstance().init(mContext)) {
            initDbDao(mContext);
        }
    }

    private void initDbDao(Context context) {
        userDao = new UserDao(context);
    }

    public static KenApplication getInstance() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void setCurrentUserName(String username) {
        this.username = username;
        UserInfo.getInstance(instance).setUserInfo(Constant.KEY_USERNAME, username);
    }

    public String getCurrentUserName() {
        if (TextUtils.isEmpty(username)) {
            username = UserInfo.getInstance(instance).getUserInfo(Constant.KEY_USERNAME);

        }
        return username;
    }

    public void setContactList(Map<String, EaseUser> contactList) {
        this.contactList = contactList;
        userDao.saveContactList(new ArrayList<EaseUser>(contactList.values()));

    }

    public Map<String, EaseUser> getContactList() {
        if (contactList == null) {
            contactList = userDao.getContactList();
        }
        return contactList;

    }
}
