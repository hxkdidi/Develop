package com.kenos.kenos.app;

import android.content.Intent;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMOptions;
import com.kenos.kenos.MainActivity;
import com.kenos.kenos.applib.Constant;
import com.kenos.kenos.applib.controller.HxManager;
import com.kenos.kenos.applib.model.HXSDKModel;
import com.kenos.kenos.applib.model.KenOsModel;
import com.kenos.kenos.domain.TopUser;
import com.kenos.kenos.domain.User;

import java.util.Map;

/**
 * User: hxk(huangxikang@520dyw.cn)
 * Date: 2016-07-15
 * Time: 16:18
 * Description:
 */
public class KenOsManager extends HxManager {

    /**
     * contact list in cache
     */
    private Map<String, User> contactList;
    private Map<String, TopUser> topUserList;

    @Override
    protected void onConnectionConflict() {
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("conflict", true);
        appContext.startActivity(intent);
    }

    @Override
    protected void onCurrentAccountRemoved() {
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.ACCOUNT_REMOVED, true);
        appContext.startActivity(intent);
    }

    @Override
    protected void registerConnectionListener() {
        super.registerConnectionListener();
    }

    @Override
    protected HXSDKModel createModel() {
        return new KenOsModel(appContext);
    }

    /**
     * get demo HX SDK Model
     */
    public KenOsModel getModel() {
        return (KenOsModel) hxModel;
    }

    /**
     * 获取内存中好友user list
     *
     * @return
     */
    public Map<String, User> getContactList() {
        if (getHXId() != null && contactList == null) {
            contactList = ((KenOsModel) getModel()).getContactList();
        }
        return contactList;
    }

    /**
     * 获取内存中置顶好友 t
     *
     * @return
     */
    public Map<String, TopUser> getTopUserList() {
        if (getHXId() != null && topUserList == null) {
            topUserList = ((KenOsModel) getModel()).getTopUserList();
        }
        return topUserList;
    }

    /**
     * 设置置顶好友到内存中
     *
     * @param topUserList
     */
    public void setTopUserList(Map<String, TopUser> topUserList) {
        this.topUserList = topUserList;
    }

    /**
     * 设置好友user list到内存中
     *
     * @param contactList
     */
    public void setContactList(Map<String, User> contactList) {
        this.contactList = contactList;
    }

    @Override
    public void logout(final EMCallBack callback) {
        super.logout(new EMCallBack() {
            @Override
            public void onSuccess() {
                setContactList(null);
                getModel().closeDB();
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onProgress(int progress, String status) {
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }
        });
    }
}
