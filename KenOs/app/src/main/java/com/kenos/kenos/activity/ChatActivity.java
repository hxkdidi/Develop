package com.kenos.kenos.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.util.VoiceRecorder;
import com.kenos.kenos.Constant;
import com.kenos.kenos.R;
import com.kenos.kenos.adapter.ExpressionAdapter;
import com.kenos.kenos.adapter.ExpressionPagerAdapter;
import com.kenos.kenos.adapter.KenMessageAdapter;
import com.kenos.kenos.base.BaseActivity;
import com.kenos.kenos.listener.VoicePlayClickListener;
import com.kenos.kenos.preference.LocalUserInfo;
import com.kenos.kenos.utils.CommonUtils;
import com.kenos.kenos.utils.EaseCommonUtils;
import com.kenos.kenos.utils.SmileUtils;
import com.kenos.kenos.view.ExpandGridView;
import com.kenos.kenos.view.PasteEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity {
    private ListView listView;
    private int chatType = 1;
    private String toChatUsername;
    private String toChatUserNick;
    private String toUserAvatar;
    private String myUserNick;
    private String myUserAvatar;
    private Button btn_send;
    private List<EMMessage> msgList;
    KenMessageAdapter adapter;
    private EMConversation conversation;
    protected int pageSize = 20;
    public static final String COPY_IMAGE = "EASEMOBIMG";
    public static int resendPos;
    private ImageView iv_back;
    private ViewPager expressionViewpager;
    private List<String> reslist;
    private View recordingContainer;
    private ImageView micImage;
    private TextView recordingHint;
    private PasteEditText et_content;
    private View buttonSetModeKeyboard;
    private View buttonSetModeVoice;
    private View buttonSend;
    private View buttonPressToSpeak;
    private LinearLayout emojiIconContainer;
    private LinearLayout btnContainer;
    private ImageView iv_emoticons_normal;
    private ImageView iv_emoticons_checked;
    private ProgressBar loadMorePB;
    private Button btnMore;
    private LinearLayout more;
    private InputMethodManager manager;
    private Drawable[] micImages;
    @SuppressLint("HandlerLeak")
    private Handler micImageHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            // 切换msg切换图片
            micImage.setImageDrawable(micImages[msg.what]);
        }
    };
    private VoiceRecorder voiceRecorder;
    private PowerManager.WakeLock wakeLock;
    public String playMsgId;
    public static final int CHAT_TYPE_SINGLE = 1;
    public static final int CHAT_TYPE_GROUP = 2;
    private TextView name;
    private boolean isloading;
    private final int pagesize = 20;
    private boolean haveMoreData = true;
    private static final int REQUEST_CODE_EMPTY_HISTORY = 2;
    public static final int REQUEST_CODE_CONTEXT_MENU = 3;
    private static final int REQUEST_CODE_MAP = 4;
    public static final int REQUEST_CODE_TEXT = 5;
    public static final int REQUEST_CODE_VOICE = 6;
    public static final int REQUEST_CODE_PICTURE = 7;
    public static final int REQUEST_CODE_LOCATION = 8;
    public static final int REQUEST_CODE_NET_DISK = 9;
    public static final int REQUEST_CODE_FILE = 10;
    public static final int REQUEST_CODE_COPY_AND_PASTE = 11;
    public static final int REQUEST_CODE_PICK_VIDEO = 12;
    public static final int REQUEST_CODE_DOWNLOAD_VIDEO = 13;
    public static final int REQUEST_CODE_VIDEO = 14;
    public static final int REQUEST_CODE_DOWNLOAD_VOICE = 15;
    public static final int REQUEST_CODE_SELECT_USER_CARD = 16;
    public static final int REQUEST_CODE_SEND_USER_CARD = 17;
    public static final int REQUEST_CODE_CAMERA = 18;
    public static final int REQUEST_CODE_LOCAL = 19;
    public static final int REQUEST_CODE_CLICK_DESTORY_IMG = 20;
    public static final int REQUEST_CODE_GROUP_DETAIL = 21;
    public static final int REQUEST_CODE_SELECT_VIDEO = 23;
    public static final int REQUEST_CODE_SELECT_FILE = 24;
    public static final int REQUEST_CODE_ADD_TO_BLACKLIST = 25;

    public static final int RESULT_CODE_COPY = 1;
    public static final int RESULT_CODE_DELETE = 2;
    public static final int RESULT_CODE_FORWARD = 3;
    public static final int RESULT_CODE_OPEN = 4;
    public static final int RESULT_CODE_DWONLOAD = 5;
    public static final int RESULT_CODE_TO_CLOUD = 6;
    public static final int RESULT_CODE_EXIT_GROUP = 7;
    private ImageView btnLocation;


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_chat);
        initView();
        setChatType();
        setListener();
        initExpression();
        initVoice();
        getAllMessage();
        msgList = conversation.getAllMessages();
        adapter = new KenMessageAdapter(msgList, toChatUsername, ChatActivity.this);
        listView.setAdapter(adapter);
        int count = listView.getCount();
        if (count > 0) {
            listView.setSelection(count - 1);
        }
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "ChatActivity");
    }

    private void initView() {
        listView = (ListView) this.findViewById(R.id.listView);
        btn_send = (Button) this.findViewById(R.id.btn_send);
        et_content = (PasteEditText) this.findViewById(R.id.et_content);
        iv_back = (ImageView) this.findViewById(R.id.iv_back);
        recordingContainer = findViewById(R.id.recording_container);
        micImage = (ImageView) findViewById(R.id.mic_image);
        recordingHint = (TextView) findViewById(R.id.recording_hint);
        buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
        buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
        buttonSend = findViewById(R.id.btn_send);
        buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
        expressionViewpager = (ViewPager) findViewById(R.id.vPager);
        emojiIconContainer = (LinearLayout) findViewById(R.id.ll_face_container);
        btnContainer = (LinearLayout) findViewById(R.id.ll_btn_container);
        iv_emoticons_normal = (ImageView) findViewById(R.id.iv_emoticons_normal);
        iv_emoticons_checked = (ImageView) findViewById(R.id.iv_emoticons_checked);
        loadMorePB = (ProgressBar) findViewById(R.id.pb_load_more);
        btnMore = (Button) findViewById(R.id.btn_more);
        iv_emoticons_normal.setVisibility(View.VISIBLE);
        iv_emoticons_checked.setVisibility(View.GONE);
        more = (LinearLayout) findViewById(R.id.more);
        btnLocation = (ImageView) findViewById(R.id.btn_location);
        //==========================================================================
        et_content.requestFocus();
        et_content.setBackgroundResource(R.drawable.edit_text_bg_normal);
        name = (TextView) findViewById(R.id.name);
        name.setText(toChatUsername);
    }

    private void setChatType() {
        // 判断单聊还是群聊
        chatType = getIntent().getIntExtra("chatType", CHAT_TYPE_SINGLE);
        if (chatType == CHAT_TYPE_SINGLE) { // 单聊
            toChatUsername = getIntent().getStringExtra("userName");
            toChatUserNick = getIntent().getStringExtra("userNick");
            ((TextView) findViewById(R.id.name)).setText(toChatUserNick);
            toChatUserNick = getIntent().getStringExtra("userNick");
            toUserAvatar = getIntent().getStringExtra("userAvatar");
            if (toChatUserNick == null) {
                toChatUserNick = toChatUsername;
            }
            if (toUserAvatar == null) {
                toUserAvatar = "0.png";
            }
        } else {
            findViewById(R.id.container_video_call).setVisibility(View.GONE);
            findViewById(R.id.container_voice_call).setVisibility(View.GONE);
            toChatUsername = getIntent().getStringExtra("userName");
            String groupName = getIntent().getStringExtra("groupName");
            ((TextView) findViewById(R.id.name)).setText(groupName);
        }
        // 读取本地自己的头像和昵称
        myUserNick = LocalUserInfo.getInstance(ChatActivity.this).getUserInfo("nick");
        myUserAvatar = LocalUserInfo.getInstance(ChatActivity.this).getUserInfo("avatar");
    }

    private void setListener() {
        iv_back.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        iv_emoticons_normal.setOnClickListener(this);
        iv_emoticons_checked.setOnClickListener(this);
        buttonSetModeKeyboard.setOnClickListener(this);
        buttonSetModeVoice.setOnClickListener(this);
        buttonPressToSpeak.setOnClickListener(this);
        more.setOnClickListener(this);
        btnMore.setOnClickListener(this);
        btnLocation.setOnClickListener(this);
        listView.setOnScrollListener(new ListScrollListener());
        listView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                more.setVisibility(View.GONE);
                iv_emoticons_normal.setVisibility(View.VISIBLE);
                iv_emoticons_checked.setVisibility(View.GONE);
                emojiIconContainer.setVisibility(View.GONE);
                btnContainer.setVisibility(View.GONE);
                return false;
            }
        });
        et_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    et_content.setBackgroundResource(R.drawable.edit_text_bg_focused);
                } else {
                    et_content.setBackgroundResource(R.drawable.edit_text_bg_normal);
                }
            }
        });
        et_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_content.setBackgroundResource(R.drawable.edit_text_bg_focused);
                more.setVisibility(View.GONE);
                iv_emoticons_normal.setVisibility(View.VISIBLE);
                iv_emoticons_checked.setVisibility(View.GONE);
                emojiIconContainer.setVisibility(View.GONE);
                btnContainer.setVisibility(View.GONE);
            }
        });
        // 监听文字框
        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    btnMore.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                } else {
                    btnMore.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_emoticons_normal:
                more.setVisibility(View.VISIBLE);
                iv_emoticons_normal.setVisibility(View.GONE);
                iv_emoticons_checked.setVisibility(View.VISIBLE);
                btnContainer.setVisibility(View.GONE);
                emojiIconContainer.setVisibility(View.VISIBLE);
                hideKeyboard();
                break;
            case R.id.iv_emoticons_checked:
                iv_emoticons_normal.setVisibility(View.VISIBLE);
                iv_emoticons_checked.setVisibility(View.GONE);
                btnContainer.setVisibility(View.VISIBLE);
                emojiIconContainer.setVisibility(View.GONE);
                more.setVisibility(View.GONE);
                break;
            case R.id.btn_send:
                String content = et_content.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                sendText(content);
                break;
            case R.id.et_content:
                listView.setSelection(listView.getCount() - 1);
                if (more.getVisibility() == View.VISIBLE) {
                    more.setVisibility(View.GONE);
                    iv_emoticons_normal.setVisibility(View.VISIBLE);
                    iv_emoticons_checked.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_set_mode_voice:
                hideKeyboard();
                et_content.setVisibility(View.GONE);
                more.setVisibility(View.GONE);
                buttonSetModeVoice.setVisibility(View.GONE);
                buttonSetModeKeyboard.setVisibility(View.VISIBLE);
                buttonSend.setVisibility(View.GONE);
                btnMore.setVisibility(View.VISIBLE);
                buttonPressToSpeak.setVisibility(View.VISIBLE);
                iv_emoticons_normal.setVisibility(View.VISIBLE);
                iv_emoticons_checked.setVisibility(View.GONE);
                btnContainer.setVisibility(View.VISIBLE);
                emojiIconContainer.setVisibility(View.GONE);
                break;
            case R.id.btn_set_mode_keyboard:
                et_content.setVisibility(View.VISIBLE);
                more.setVisibility(View.GONE);
                buttonSetModeKeyboard.setVisibility(View.GONE);
                buttonSetModeVoice.setVisibility(View.VISIBLE);
                et_content.requestFocus();
                buttonPressToSpeak.setVisibility(View.GONE);
                if (TextUtils.isEmpty(et_content.getText())) {
                    btnMore.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                } else {
                    btnMore.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_more:
                if (more.getVisibility() == View.GONE) {
                    System.out.println("more gone");
                    hideKeyboard();
                    more.setVisibility(View.VISIBLE);
                    btnContainer.setVisibility(View.VISIBLE);
                    emojiIconContainer.setVisibility(View.GONE);
                } else {
                    if (emojiIconContainer.getVisibility() == View.VISIBLE) {
                        emojiIconContainer.setVisibility(View.GONE);
                        btnContainer.setVisibility(View.VISIBLE);
                        iv_emoticons_normal.setVisibility(View.VISIBLE);
                        iv_emoticons_checked.setVisibility(View.GONE);
                    } else {
                        more.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.btn_location:
                startActivityForResult(new Intent(this, BaiduMapActivity.class), REQUEST_CODE_MAP);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            finish();
        }

        if (resultCode == RESULT_CODE_EXIT_GROUP) {//退群
            setResult(RESULT_OK);
            finish();
            return;
        }
        if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
            switch (resultCode) {
                case RESULT_CODE_COPY: // 复制消息

                    break;
                case RESULT_CODE_DELETE: // 删除消息

                    break;
                case RESULT_CODE_FORWARD: // 转发消息

                    break;

                default:
                    break;
            }
        }
        if (resultCode == RESULT_OK) { // 清空消息
            if (requestCode == REQUEST_CODE_EMPTY_HISTORY) {

            } else if (requestCode == REQUEST_CODE_CAMERA) { // 发送照片

            } else if (requestCode == REQUEST_CODE_SELECT_VIDEO) { // 发送本地选择的视频


            } else if (requestCode == REQUEST_CODE_LOCAL) { // 发送本地图片

            } else if (requestCode == REQUEST_CODE_SELECT_FILE) { // 发送选择的文件


            } else if (requestCode == REQUEST_CODE_MAP) { // 地图
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                String locationAddress = data.getStringExtra("address");
                if (locationAddress != null && !locationAddress.equals("")) {
                    sendLocationMsg(latitude, longitude, locationAddress);
                } else {
                    Toast.makeText(this, "无法获取到您的位置信息！", Toast.LENGTH_SHORT).show();
                }
                // 重发消息
            } else if (requestCode == REQUEST_CODE_TEXT
                    || requestCode == REQUEST_CODE_VOICE
                    || requestCode == REQUEST_CODE_PICTURE
                    || requestCode == REQUEST_CODE_LOCATION
                    || requestCode == REQUEST_CODE_VIDEO
                    || requestCode == REQUEST_CODE_FILE) {

            } else if (requestCode == REQUEST_CODE_COPY_AND_PASTE) {//粘贴

            } else if (requestCode == REQUEST_CODE_ADD_TO_BLACKLIST) { // 移入黑名单

            } else if (conversation.getAllMsgCount() > 0) {//有消息

            } else if (requestCode == REQUEST_CODE_GROUP_DETAIL) {//群组详情

            }
        }
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * listview滑动监听listener
     */
    private class ListScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    if (view.getFirstVisiblePosition() == 0 && !isloading
                            && haveMoreData) {
                        loadMorePB.setVisibility(View.VISIBLE);
                        // sdk初始化加载的聊天记录为20条，到顶时去db里获取更多
                        List<EMMessage> messages;
                        try {
                            // 获取更多messges，调用此方法的时候从db获取的messages
                            // sdk会自动存入到此conversation中
                            if (chatType == CHAT_TYPE_SINGLE)
                                messages = conversation.loadMoreMsgFromDB(adapter.getItem(0).getMsgId(), pagesize);
                            else
                                messages = conversation.loadMoreMsgFromDB(adapter.getItem(0).getMsgId(), pagesize);
                        } catch (Exception e1) {
                            loadMorePB.setVisibility(View.GONE);
                            return;
                        }
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                        }
                        if (messages.size() != 0) {
                            // 刷新ui
                            adapter.notifyDataSetChanged();
                            listView.setSelection(messages.size() - 1);
                            if (messages.size() != pagesize)
                                haveMoreData = false;
                        } else {
                            haveMoreData = false;
                        }
                        loadMorePB.setVisibility(View.GONE);
                        isloading = false;
                    }
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        }

    }

    protected void getAllMessage() {
        // 获取当前conversation对象
        conversation = EMClient.getInstance().chatManager().getConversation(toChatUsername, EaseCommonUtils.getConversationType(chatType), true);
        // 把此会话的未读数置为0
        conversation.markAllMessagesAsRead();
        // 初始化db时，每个conversation加载数目是getChatOptions().getNumberOfMessagesLoaded
        // 这个数目如果比用户期望进入会话界面时显示的个数不一样，就多加载一些
        final List<EMMessage> msgs = conversation.getAllMessages();
        int msgCount = msgs != null ? msgs.size() : 0;
        if (msgCount < conversation.getAllMsgCount() && msgCount < pageSize) {
            String msgId = null;
            if (msgs != null && msgs.size() > 0) {
                msgId = msgs.get(0).getMsgId();
            }
            conversation.loadMoreMsgFromDB(msgId, pageSize - msgCount);
        }
    }

    /**
     * 发送文本消息
     *
     * @param content
     */
    private void sendText(String content) {
        // 创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        // 如果是群聊，设置chattype，默认是单聊
        if (chatType == Constant.CHATTYPE_GROUP) {
            message.setChatType(ChatType.GroupChat);
        }
        // 发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
        msgList.add(message);
        adapter.notifyDataSetChanged();
        if (msgList.size() > 0) {
            listView.setSelection(listView.getCount() - 1);
        }
        et_content.setText("");
        et_content.clearFocus();
    }

    /**
     * 发送语音
     *
     * @param filePath
     * @param length
     */
    private void sendVoice(String filePath, String length) {
        if (!(new File(filePath).exists())) {
            return;
        }
        try {
            final EMMessage message = EMMessage.createSendMessage(EMMessage.Type.VOICE);
            if (chatType == CHAT_TYPE_GROUP) message.setChatType(ChatType.GroupChat);
            message.setReceipt(toChatUsername);
            message.setAttribute("toUserNick", toChatUserNick);
            message.setAttribute("toUserAvatar", toUserAvatar);
            message.setAttribute("useravatar", myUserAvatar);
            message.setAttribute("usernick", myUserNick);
            int len = Integer.parseInt(length);
            EMVoiceMessageBody body = new EMVoiceMessageBody(new File(filePath), len);
            message.addBody(body);
            msgList.add(message);
            adapter.notifyDataSetChanged();
            listView.setSelection(listView.getCount() - 1);
            setResult(RESULT_OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送位置信息
     *
     * @param latitude
     * @param longitude
     * @param locationAddress
     */
    private void sendLocationMsg(double latitude, double longitude, String locationAddress) {
        EMMessage message = EMMessage.createSendMessage(EMMessage.Type.LOCATION);
        if (chatType == CHAT_TYPE_GROUP) message.setChatType(ChatType.GroupChat);
        EMLocationMessageBody locBody = new EMLocationMessageBody(locationAddress, latitude, longitude);
        message.addBody(locBody);
        message.setReceipt(toChatUsername);
        message.setAttribute("toUserNick", toChatUserNick);
        message.setAttribute("toUserAvatar", toUserAvatar);
        message.setAttribute("useravatar", myUserAvatar);
        message.setAttribute("usernick", myUserNick);
        msgList.add(message);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setSelection(listView.getCount() - 1);
        setResult(RESULT_OK);

    }

    /**
     * 发送消息回调
     */
    EMMessageListener msgListener = new EMMessageListener() {
        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            for (EMMessage message : messages) {
                String username;
                // 群组消息
                if (message.getChatType() == ChatType.GroupChat || message.getChatType() == ChatType.ChatRoom) {
                    username = message.getTo();
                } else {
                    // 单聊消息
                    username = message.getFrom();
                }
                // 如果是当前会话的消息，刷新聊天页面
                if (username.equals(toChatUsername)) {
                    msgList.addAll(messages);
                    adapter.notifyDataSetChanged();
                    if (msgList.size() > 0) {
                        et_content.setSelection(listView.getCount() - 1);
                    }
                }
            }
            // 收到消息
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {
            // 收到透传消息
        }

        @Override
        public void onMessageReadAckReceived(List<EMMessage> messages) {
            // 收到已读回执
        }

        @Override
        public void onMessageDeliveryAckReceived(List<EMMessage> message) {
            // 收到已送达回执
        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {
            // 消息状态变动
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // 点击notification bar进入聊天页面，保证只有一个聊天页面
        String username = intent.getStringExtra("userId");
        if (toChatUsername.equals(username))
            super.onNewIntent(intent);
        else {
            finish();
            startActivity(intent);
        }
    }

    /**
     * 手机返回键
     */
    @Override
    public void onBackPressed() {
        if (more.getVisibility() == View.VISIBLE) {
            more.setVisibility(View.GONE);
            iv_emoticons_normal.setVisibility(View.VISIBLE);
            iv_emoticons_checked.setVisibility(View.GONE);
        } else {
            finish();
            super.onBackPressed();
        }
    }

    private void initExpression() {
        // 表情list
        reslist = getExpressionRes(35);
        // 初始化表情viewpager
        List<View> views = new ArrayList<>();
        View gv1 = getGridChildView(1);
        View gv2 = getGridChildView(2);
        views.add(gv1);
        views.add(gv2);
        expressionViewpager.setAdapter(new ExpressionPagerAdapter(views));
    }

    public List<String> getExpressionRes(int getSum) {
        List<String> reslist = new ArrayList<>();
        for (int x = 1; x <= getSum; x++) {
            String filename = "ee_" + x;
            reslist.add(filename);
        }
        return reslist;
    }

    /**
     * 获取表情的gridview的子view
     *
     * @param i
     * @return
     */
    private View getGridChildView(int i) {
        View view = View.inflate(this, R.layout.expression_gridview, null);
        ExpandGridView gv = (ExpandGridView) view.findViewById(R.id.gridview);
        List<String> list = new ArrayList<>();
        if (i == 1) {
            List<String> list1 = reslist.subList(0, 20);
            list.addAll(list1);
        } else if (i == 2) {
            list.addAll(reslist.subList(20, reslist.size()));
        }
        list.add("delete_expression");
        final ExpressionAdapter expressionAdapter = new ExpressionAdapter(this, 1, list);
        gv.setAdapter(expressionAdapter);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filename = expressionAdapter.getItem(position);
                try {
                    // 文字输入框可见时，才可输入表情
                    // 按住说话可见，不让输入表情
                    if (buttonSetModeKeyboard.getVisibility() != View.VISIBLE) {
                        if (filename != "delete_expression") { // 不是删除键，显示表情
                            // 这里用的反射，所以混淆的时候不要混淆SmileUtils这个类
                            @SuppressWarnings("rawtypes")
                            Class clz = Class.forName("com.kenos.kenos.utils.SmileUtils");
                            Field field = clz.getField(filename);
                            et_content.append(SmileUtils.getSmiledText(ChatActivity.this, (String) field.get(null)));
                        } else { // 删除文字或者表情
                            if (!TextUtils.isEmpty(et_content.getText())) {
                                int selectionStart = et_content.getSelectionStart();// 获取光标的位置
                                if (selectionStart > 0) {
                                    String body = et_content.getText().toString();
                                    String tempStr = body.substring(0, selectionStart);
                                    int i = tempStr.lastIndexOf("[");// 获取最后一个表情的位置
                                    if (i != -1) {
                                        CharSequence cs = tempStr.substring(i, selectionStart);
                                        if (SmileUtils.containsKey(cs.toString()))
                                            et_content.getEditableText().delete(i, selectionStart);
                                        else
                                            et_content.getEditableText().delete(selectionStart - 1, selectionStart);
                                    } else {
                                        et_content.getEditableText().delete(selectionStart - 1, selectionStart);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        });
        return view;
    }

    /**
     * 初始化语音
     */
    private void initVoice() {
        // 动画资源文件,用于录制语音时
        micImages = new Drawable[]{
                getResources().getDrawable(R.mipmap.record_animate_01),
                getResources().getDrawable(R.mipmap.record_animate_02),
                getResources().getDrawable(R.mipmap.record_animate_03),
                getResources().getDrawable(R.mipmap.record_animate_04),
                getResources().getDrawable(R.mipmap.record_animate_05),
                getResources().getDrawable(R.mipmap.record_animate_06),
                getResources().getDrawable(R.mipmap.record_animate_07),
                getResources().getDrawable(R.mipmap.record_animate_08),
                getResources().getDrawable(R.mipmap.record_animate_09),
                getResources().getDrawable(R.mipmap.record_animate_10),
                getResources().getDrawable(R.mipmap.record_animate_11),
                getResources().getDrawable(R.mipmap.record_animate_12),
                getResources().getDrawable(R.mipmap.record_animate_13),
                getResources().getDrawable(R.mipmap.record_animate_14),};
        voiceRecorder = new VoiceRecorder(micImageHandler);
        buttonPressToSpeak.setOnTouchListener(new PressToSpeakListener());
    }

    /**
     * 按住说话listener
     */
    class PressToSpeakListener implements View.OnTouchListener {
        @SuppressLint({"ClickableViewAccessibility", "Wakelock"})
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!CommonUtils.isExitsSdcard()) {
                        Toast.makeText(ChatActivity.this, "发送语音需要sdcard支持！", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    try {
                        v.setPressed(true);
                        wakeLock.acquire();
                        if (VoicePlayClickListener.isPlaying)
                            VoicePlayClickListener.currentPlayListener.stopPlayVoice();
                        recordingContainer.setVisibility(View.VISIBLE);
                        recordingHint.setText(getString(R.string.move_up_to_cancel));
                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
                        voiceRecorder.startRecording(null, toChatUsername, getApplicationContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                        v.setPressed(false);
                        if (wakeLock.isHeld())
                            wakeLock.release();
                        if (voiceRecorder != null)
                            voiceRecorder.discardRecording();
                        recordingContainer.setVisibility(View.INVISIBLE);
                        Toast.makeText(ChatActivity.this, R.string.recoding_fail,
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (event.getY() < 0) {
                        recordingHint.setText(getString(R.string.release_to_cancel));
                        recordingHint.setBackgroundResource(R.drawable.recording_text_hint_bg);
                    } else {
                        recordingHint.setText(getString(R.string.move_up_to_cancel));
                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    v.setPressed(false);
                    recordingContainer.setVisibility(View.INVISIBLE);
                    if (wakeLock.isHeld())
                        wakeLock.release();
                    if (event.getY() < 0) {
                        // discard the recorded audio.
                        voiceRecorder.discardRecording();
                    } else {
                        // stop recording and send voice file
                        try {
                            int length = voiceRecorder.stopRecoding();
                            if (length > 0) {
                                sendVoice(voiceRecorder.getVoiceFilePath(), Integer.toString(length));
                            } else if (length == EMError.FILE_INVALID) {
                                Toast.makeText(getApplicationContext(), "无录音权限", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "录音时间太短", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ChatActivity.this, "发送失败，请检测服务器是否连接", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                default:
                    recordingContainer.setVisibility(View.INVISIBLE);
                    if (voiceRecorder != null)
                        voiceRecorder.discardRecording();
                    return false;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wakeLock.isHeld())
            wakeLock.release();
        if (VoicePlayClickListener.isPlaying && VoicePlayClickListener.currentPlayListener != null) {
            // 停止语音播放
            VoicePlayClickListener.currentPlayListener.stopPlayVoice();
        }
        try {
            // 停止录音
            if (voiceRecorder.isRecording()) {
                voiceRecorder.discardRecording();
                recordingContainer.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
        }
    }
}
