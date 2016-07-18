package com.kenos.kenos.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMContactListener;
import com.hyphenate.chat.EMClient;
import com.kenos.kenos.R;
import com.kenos.kenos.app.KenApplication;
import com.kenos.kenos.base.BaseActivity;
import com.kenos.kenos.chat.KenOsManager;
import com.kenos.kenos.db.EaseUser;
import com.kenos.kenos.db.InviteMessage;
import com.kenos.kenos.db.InviteMessageDao;
import com.kenos.kenos.db.UserDao;
import com.kenos.kenos.fragment.Fragment_Discover;
import com.kenos.kenos.fragment.Fragment_Friends;
import com.kenos.kenos.fragment.Fragment_Message;
import com.kenos.kenos.fragment.Fragment_Profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class MainActivity extends BaseActivity {
    private FragmentActivity mContext;
    private TextView tv_title;
    private TextView unreaMsgdLabel;// 未读消息
    private TextView unreadAddressLable;// 未读通讯录
    private TextView unreadFindLable;// 未读发现
    private Fragment[] fragments;
    public Fragment_Message homeFragment;
    private Fragment_Friends contactListFragment;
    private Fragment_Discover findFragment;
    private Fragment_Profile profileFragment;
    private ImageView[] imagebuttons;
    private TextView[] textviews;
    private int index;
    private int currentTabIndex;// 当前fragment的index
    private InviteMessageDao mInviteMessageDao;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        setContentView(R.layout.activity_main);
        initTabView();
    }

    private void initTabView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        unreaMsgdLabel = (TextView) findViewById(R.id.unread_msg_number);
        unreadAddressLable = (TextView) findViewById(R.id.unread_address_number);
        unreadFindLable = (TextView) findViewById(R.id.unread_find_number);
        homeFragment = new Fragment_Message();
        contactListFragment = new Fragment_Friends();
        findFragment = new Fragment_Discover();
        profileFragment = new Fragment_Profile();
        fragments = new Fragment[]{homeFragment, contactListFragment, findFragment, profileFragment};
        imagebuttons = new ImageView[4];
        imagebuttons[0] = (ImageView) findViewById(R.id.ib_weixin);
        imagebuttons[1] = (ImageView) findViewById(R.id.ib_contact_list);
        imagebuttons[2] = (ImageView) findViewById(R.id.ib_find);
        imagebuttons[3] = (ImageView) findViewById(R.id.ib_profile);

        imagebuttons[0].setSelected(true);
        textviews = new TextView[4];
        textviews[0] = (TextView) findViewById(R.id.tv_weixin);
        textviews[1] = (TextView) findViewById(R.id.tv_contact_list);
        textviews[2] = (TextView) findViewById(R.id.tv_find);
        textviews[3] = (TextView) findViewById(R.id.tv_profile);
        textviews[0].setTextColor(0xFF45C01A);
        // 添加显示第一个fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment)
                .add(R.id.fragment_container, contactListFragment)
                .add(R.id.fragment_container, profileFragment)
                .add(R.id.fragment_container, findFragment)
                .hide(contactListFragment).hide(profileFragment)
                .hide(findFragment).show(homeFragment).commit();
        mInviteMessageDao = new InviteMessageDao(mContext);
        userDao = new UserDao(mContext);
        //注册联系人变动监听
        EMClient.getInstance().contactManager().setContactListener(new MyContactListener());
    }

    public void onTabClicked(View view) {
        switch (view.getId()) {
            case R.id.re_weixin:
                index = 0;
                tv_title.setText("微信");
                break;
            case R.id.re_contact_list:
                index = 1;
                tv_title.setText("通讯录");
                break;
            case R.id.re_find:
                index = 2;
                tv_title.setText("发现");
                break;
            case R.id.re_profile:
                index = 3;
                tv_title.setText("我");
                break;
            case R.id.tv_exit:
               logout();
                break;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
        }
        imagebuttons[currentTabIndex].setSelected(false);
        // 把当前tab设为选中状态
        imagebuttons[index].setSelected(true);
        textviews[currentTabIndex].setTextColor(0xFF999999);
        textviews[index].setTextColor(0xFF45C01A);
        currentTabIndex = index;
    }

    /***
     * 好友变化listener
     */
    public class MyContactListener implements EMContactListener {

        @Override
        public void onContactAdded(final String username) {
            // 保存增加的联系人
            Map<String, EaseUser> localUsers = KenApplication.getInstance().getContactList();
            Map<String, EaseUser> toAddUsers = new HashMap<>();
            EaseUser user = new EaseUser(username);
            // 添加好友时可能会回调added方法两次
            if (!localUsers.containsKey(username)) {
                userDao.saveContact(user);
            }
            toAddUsers.put(username, user);
            localUsers.putAll(toAddUsers);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "增加联系人：+" + username, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onContactDeleted(final String username) {
            // 被删除
            Map<String, EaseUser> localUsers = KenApplication.getInstance().getContactList();
            localUsers.remove(username);
            userDao.deleteContact(username);
            mInviteMessageDao.deleteMessage(username);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "删除联系人：+" + username, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onContactInvited(final String username, String reason) {
            // 接到邀请的消息，如果不处理(同意或拒绝)，掉线后，服务器会自动再发过来，所以客户端不需要重复提醒
            List<InviteMessage> msgs = mInviteMessageDao.getMessagesList();
            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getGroupId() == null && inviteMessage.getFrom().equals(username)) {
                    mInviteMessageDao.deleteMessage(username);
                }
            }
            // 自己封装的javabean
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            msg.setReason(reason);
            // 设置相应status
            msg.setStatus(InviteMessage.InviteMesageStatus.BEINVITEED);
            notifyNewIviteMessage(msg);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "收到好友申请：+" + username, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onContactAgreed(final String username) {
            List<InviteMessage> msgs = mInviteMessageDao.getMessagesList();
            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getFrom().equals(username)) {
                    return;
                }
            }
            // 自己封装的javabean
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            msg.setStatus(InviteMessage.InviteMesageStatus.BEAGREED);
            notifyNewIviteMessage(msg);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "好友申请同意：+" + username, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onContactRefused(String username) {
            // 参考同意，被邀请实现此功能,demo未实现
            Log.d(username, username + "拒绝了你的好友请求");
        }
    }

    /**
     * 保存并提示消息的邀请消息
     *
     * @param msg
     */
    private void notifyNewIviteMessage(InviteMessage msg) {
        if (mInviteMessageDao == null) {
            mInviteMessageDao = new InviteMessageDao(mContext);
        }
        mInviteMessageDao.saveMessage(msg);
        //保存未读数，这里没有精确计算
        mInviteMessageDao.saveUnreadMessageCount(1);
        // 提示有新消息
        //响铃或其他操作
    }

    private void logout() {
        final ProgressDialog pd = new ProgressDialog(mContext);
        String st = getResources().getString(R.string.Are_logged_out);
        pd.setMessage(st);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        KenOsManager.getInstance().logout(false, new EMCallBack() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        pd.dismiss();
                        // 重新显示登陆页面
                        finish();
                        startActivity(new Intent(mContext, LoginActivity.class));
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        Toast.makeText(mContext, "unbind devicetokens failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
