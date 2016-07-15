package com.kenos.kenos.applib.controller;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.kenos.kenos.applib.model.DefaultHXSDKModel;
import com.kenos.kenos.applib.model.HXSDKModel;

import java.util.Iterator;
import java.util.List;

/**
 * User: hxk(huangxikang@520dyw.cn)
 * Date: 2016-07-15
 * Time: 16:19
 * Description:
 */
public abstract class HxManager {
    private static final String TAG = "HXSDKManager";
    /**
     * application context
     */
    protected Context appContext = null;

    /**
     * HuanXin mode helper, which will manage the user data and user preferences
     */
    protected HXSDKModel hxModel = null;

    /**
     * MyConnectionListener
     */
    protected EMConnectionListener connectionListener = null;

    /**
     * HuanXin ID in cache
     */
    protected String hxId = null;

    /**
     * password in cache
     */
    protected String password = null;

    /**
     * init flag: test if the sdk has been inited before, we don't need to init again
     */
    private boolean sdkInited = false;

    /**
     * the global HXSDKManager instance
     */
    private static HxManager me = null;

    public HxManager() {
        me = this;
    }

    /**
     * this function will initialize the HuanXin SDK
     *
     * @return boolean true if caller can continue to call HuanXin related APIs after calling onInit, otherwise false.
     * <p/>
     * 环信初始化SDK帮助函数
     * 返回true如果正确初始化，否则false，如果返回为false，请在后续的调用中不要调用任何和环信相关的代码
     * <p/>
     * for example:
     * 例子：
     * <p/>
     * public class FXManager extends HXSDKManager
     * <p/>
     * HXHelper = new FXManager();
     * if(HXHelper.onInit(context)){
     * // do HuanXin related work
     * }
     */
    public synchronized boolean onInit(Context context, EMOptions options) {
        if (sdkInited) {
            return true;
        }
        appContext = context;
        // create HX SDK model
        hxModel = createModel();

        // create a defalut HX SDK model in case subclass did not provide the model
        if (hxModel == null) {
            hxModel = new DefaultHXSDKModel(appContext);
        }

        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);

        Log.e("ewrwetwetwet", "process app name : " + processAppName);

        // 如果app启用了远程的service，此application:onCreate会被调用2次
        // 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
        // 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回
        if (processAppName == null || !processAppName.equalsIgnoreCase(hxModel.getAppProcessName())) {
            Log.e(TAG, "enter the service process!");
            // 则此application::onCreate 是被service 调用的，直接返回
            return false;
        }

        if (options == null) {
            EMClient.getInstance().init(context, initHXOptions());
        } else {
            // 初始化环信SDK,一定要先调用init()
            EMClient.getInstance().init(context, options);
        }
        if (hxModel.isDebugMode()) {
            // set debug mode in development process
            EMClient.getInstance().setDebugMode(true);
        }
        Log.d(TAG, "initialize EMChat SDK");
        registerConnectionListener();
        registerMessageListener();
        sdkInited = true;
        return true;
    }

    private void registerMessageListener() {
        EMClient.getInstance().chatManager().addMessageListener(new EMMessageListener() {

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                //处理新消息
                Toast.makeText(appContext, "一条新消息来了", Toast.LENGTH_LONG);
            }

            @Override
            public void onMessageReadAckReceived(List<EMMessage> messages) {

            }

            @Override
            public void onMessageDeliveryAckReceived(List<EMMessage> messages) {
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {

            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {

            }
        });
    }

    /**
     * get global instance
     *
     * @return
     */
    public static HxManager getInstance() {
        return me;
    }

    public HXSDKModel getModel() {
        return hxModel;
    }

    public String getHXId() {
        if (hxId == null) {
            hxId = hxModel.getHXId();
        }
        return hxId;
    }

    public String getPassword() {
        if (password == null) {
            password = hxModel.getPwd();
        }
        return password;
    }

    public void setHXId(String hxId) {
        if (hxId != null) {
            if (hxModel.saveHXId(hxId)) {
                this.hxId = hxId;
            }
        }
    }

    public void setPassword(String password) {
        if (hxModel.savePassword(password)) {
            this.password = password;
        }
    }

    /**
     * the subclass must override this class to provide its own model or directly use {@link}
     *
     * @return
     */
    abstract protected HXSDKModel createModel();

    /**
     * please make sure you have to get EMChatOptions by following method and set related options
     * EMChatOptions options = EMChatManager.getInstance().getChatOptions();
     */
    protected EMOptions initHXOptions() {
        Log.d(TAG, "init HuanXin Options");
        // 获取到EMChatOptions对象
        EMOptions options = new EMOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(hxModel.getAcceptInvitationAlways());
        // 设置是否需要已读回执
        options.setRequireAck(hxModel.getRequireReadAck());
        // 设置是否需要已送达回执
        options.setRequireDeliveryAck(hxModel.getRequireDeliveryAck());
        return options;
    }

    /**
     * logout HuanXin SDK
     */
    public void logout(final EMCallBack callback) {
        EMClient.getInstance().logout(true, new EMCallBack() {

            @Override
            public void onSuccess() {
                setPassword(null);
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

    /**
     * 检查是否已经登录过
     *
     * @return
     */
    public boolean isLogined() {
        if (hxModel.getHXId() != null && hxModel.getPwd() != null) {
            return true;
        }
        return false;
    }

    /**
     * init HuanXin listeners
     */
    protected void registerConnectionListener() {
        Log.d(TAG, "init listener");
        // create the global connection listener
        connectionListener = new EMConnectionListener() {
            @Override
            public void onDisconnected(int error) {
                //账号被移除
                if (error == EMError.USER_REMOVED) {
                    onCurrentAccountRemoved();
                    //账号在其他地方登陆
                } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                    onConnectionConflict();
                } else {
                    //连接不到聊天服务器
                    onConnectionDisconnected(error);
                }
            }

            @Override
            public void onConnected() {
                onConnectionConnected();
            }
        };
        EMClient.getInstance().addConnectionListener(connectionListener);
    }

    /**
     * the developer can override this function to handle connection conflict error
     */
    protected void onConnectionConflict() {
    }


    /**
     * the developer can override this function to handle user is removed error
     */
    protected void onCurrentAccountRemoved() {
    }


    /**
     * handle the connection connected
     */
    protected void onConnectionConnected() {
    }

    /**
     * handle the connection disconnect
     *
     * @param error see {@link EMError}
     */
    protected void onConnectionDisconnected(int error) {
    }

    /**
     * check the application process name if process name is not qualified, then we think it is a service process and we will not init SDK
     *
     * @param pID
     * @return
     */
    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = appContext.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    // Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
                    // info.processName +"  Label: "+c.toString());
                    // processName = c.toString();
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }
}
