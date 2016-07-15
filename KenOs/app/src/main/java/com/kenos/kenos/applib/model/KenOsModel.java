package com.kenos.kenos.applib.model;

import android.content.Context;

import com.kenos.kenos.db.DbOpenHelper;
import com.kenos.kenos.db.TopUserDao;
import com.kenos.kenos.db.UserDao;
import com.kenos.kenos.domain.TopUser;
import com.kenos.kenos.domain.User;

import java.util.List;
import java.util.Map;

/**
 * User: hxk(huangxikang@520dyw.cn)
 * Date: 2016-07-15
 * Time: 17:33
 * Description:
 */
public class KenOsModel extends DefaultHXSDKModel {
    public KenOsModel(Context ctx) {
        super(ctx);
    }

    // demo will use HuanXin roster
    public boolean getUseHXRoster() {
        return true;
    }

    // demo will switch on debug mode
    public boolean isDebugMode(){
        return true;
    }

    public boolean saveContactList(List<User> contactList) {
        UserDao dao = new UserDao(context);
        dao.saveContactList(contactList);
        return true;
    }

    public Map<String, User> getContactList() {
        UserDao dao = new UserDao(context);
        return dao.getContactList();
    }
    public Map<String, TopUser> getTopUserList() {
        TopUserDao dao = new TopUserDao(context);
        return dao.getTopUserList();
    }
    public boolean saveTopUserList(List<TopUser> contactList) {
        TopUserDao dao = new TopUserDao(context);
        dao.saveTopUserList(contactList);
        return true;
    }


    public void closeDB() {
        DbOpenHelper.getInstance(context).closeDB();
    }

    @Override
    public String getAppProcessName() {
        return "com.fanxin.app";
    }
}
