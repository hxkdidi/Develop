package com.kenos.kenos.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMTextMessageBody;
import com.kenos.kenos.Constant;
import com.kenos.kenos.R;
import com.kenos.kenos.adapter.ExpressionAdapter;
import com.kenos.kenos.adapter.ExpressionPagerAdapter;
import com.kenos.kenos.base.BaseActivity;
import com.kenos.kenos.utils.EaseCommonUtils;
import com.kenos.kenos.utils.SmileUtils;
import com.kenos.kenos.view.ExpandGridView;
import com.kenos.kenos.view.PasteEditText;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity {
    private ListView listView;
    private int chatType = 1;
    private String toChatUsername;
    private Button btn_send;
    private List<EMMessage> msgList;
    MessageAdapter adapter;
    private EMConversation conversation;
    protected int pageSize = 20;
    public static final String COPY_IMAGE = "EASEMOBIMG";
    public static final int REQUEST_CODE_COPY_AND_PASTE = 11;
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
    private ProgressBar loadmorePB;
    private Button btnMore;
    private LinearLayout more;
    private InputMethodManager manager;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_chat);
        toChatUsername = this.getIntent().getStringExtra("username");
        TextView name = (TextView) this.findViewById(R.id.name);
        name.setText(toChatUsername);
        initView();
        setListener();
        initExpression();
        getAllMessage();
        msgList = conversation.getAllMessages();
        adapter = new MessageAdapter(msgList, ChatActivity.this);
        listView.setAdapter(adapter);
        int count = listView.getCount();
        if (count > 0) {
            listView.setSelection(count - 1);
        }
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
        loadmorePB = (ProgressBar) findViewById(R.id.pb_load_more);
        btnMore = (Button) findViewById(R.id.btn_more);
        iv_emoticons_normal.setVisibility(View.VISIBLE);
        iv_emoticons_checked.setVisibility(View.GONE);
        more = (LinearLayout) findViewById(R.id.more);
        //==========================================================================
        et_content.requestFocus();
        et_content.setBackgroundResource(R.drawable.edit_text_bg_normal);
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
                setMesaage(content);
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
            case R.id.btn_press_to_speak:

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
     * listView滑动监听listener
     */
    private class ListScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
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

    private void setMesaage(String content) {
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

    @SuppressLint("InflateParams")
    class MessageAdapter extends BaseAdapter {
        private List<EMMessage> msgs;
        private Context context;
        private LayoutInflater inflater;

        public MessageAdapter(List<EMMessage> msgs, Context context_) {
            this.msgs = msgs;
            this.context = context_;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return msgs.size();
        }

        @Override
        public EMMessage getItem(int position) {
            return msgs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            EMMessage message = getItem(position);
            return message.direct() == EMMessage.Direct.RECEIVE ? 0 : 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            EMMessage message = getItem(position);
            int viewType = getItemViewType(position);
            ViewHolderLeft leftViewHolder = null;
            ViewHolderRight rightViewHolder = null;
            if (convertView == null) {
                if (viewType == 0) {
                    convertView = inflater.inflate(R.layout.item_message_received, parent, false);
                    leftViewHolder = new ViewHolderLeft();
                    leftViewHolder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                    convertView.setTag(leftViewHolder);

                } else if (viewType == 1) {
                    convertView = inflater.inflate(R.layout.item_message_sent, parent, false);
                    rightViewHolder = new ViewHolderRight();
                    rightViewHolder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                    convertView.setTag(rightViewHolder);
                }
            } else {
                if (viewType == 0) {
                    leftViewHolder = (ViewHolderLeft) convertView.getTag();
                } else if (viewType == 1) {
                    rightViewHolder = (ViewHolderRight) convertView.getTag();
                }
            }
            EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
            // 设置内容
            Spannable span = SmileUtils.getSmiledText(context, txtBody.getMessage());
            if (viewType == 0) {
                leftViewHolder.tv.setText(span, TextView.BufferType.SPANNABLE);
            } else if (viewType == 1) {
                rightViewHolder.tv.setText(span, TextView.BufferType.SPANNABLE);
            }
            return convertView;
        }
    }

    public static class ViewHolderLeft {
        TextView tv;
    }

    public static class ViewHolderRight {
        TextView tv;
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
}
