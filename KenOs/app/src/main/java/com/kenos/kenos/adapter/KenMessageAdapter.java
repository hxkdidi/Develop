package com.kenos.kenos.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.DateUtils;
import com.kenos.kenos.Constant;
import com.kenos.kenos.R;
import com.kenos.kenos.activity.ChatActivity;
import com.kenos.kenos.listener.VoicePlayClickListener;
import com.kenos.kenos.utils.SmileUtils;
import com.kenos.kenos.view.KenAlertDialog;

import java.util.Date;
import java.util.List;

/**
 * User: hxk(huangxikang@520dyw.cn)
 * Date: 2016-07-20
 * Time: 11:25
 * Description:
 */
public class KenMessageAdapter extends BaseAdapter {
    private final static String TAG = "msg";
    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
    private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
    private static final int MESSAGE_TYPE_SENT_VOICE = 6;
    private static final int MESSAGE_TYPE_RECV_VOICE = 7;
    private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
    private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
    private static final int MESSAGE_TYPE_SENT_FILE = 10;
    private static final int MESSAGE_TYPE_RECV_FILE = 11;
    private static final int MESSAGE_TYPE_SENT_VOICE_CALL = 12;
    private static final int MESSAGE_TYPE_RECV_VOICE_CALL = 13;
    private static final int MESSAGE_TYPE_SENT_VIDEO_CALL = 14;
    private static final int MESSAGE_TYPE_RECV_VIDEO_CALL = 15;
    public static final String IMAGE_DIR = "chat/image/";
    public static final String VOICE_DIR = "chat/audio/";
    public static final String VIDEO_DIR = "chat/video";
    private List<EMMessage> msgs;
    private Context context;
    private LayoutInflater inflater;
    private Activity activity;
    private String username;

    public KenMessageAdapter(List<EMMessage> msgs, String username, Context context_) {
        this.msgs = msgs;
        this.username = username;
        this.context = context_;
        activity = (Activity) context;
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

    /**
     * 获取item类型
     */
    public int getItemViewType(int position) {
        EMMessage message = msgs.get(position);
        if (message.getType() == EMMessage.Type.TXT) {
            if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false))
                return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE_CALL : MESSAGE_TYPE_SENT_VOICE_CALL;
            else if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VIDEO_CALL, false))
                return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO_CALL : MESSAGE_TYPE_SENT_VIDEO_CALL;
            return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_TXT : MESSAGE_TYPE_SENT_TXT;
        }
        if (message.getType() == EMMessage.Type.IMAGE) {
            return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_IMAGE : MESSAGE_TYPE_SENT_IMAGE;
        }
        if (message.getType() == EMMessage.Type.LOCATION) {
            return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_LOCATION : MESSAGE_TYPE_SENT_LOCATION;
        }
        if (message.getType() == EMMessage.Type.VOICE) {
            return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE : MESSAGE_TYPE_SENT_VOICE;
        }
        if (message.getType() == EMMessage.Type.VIDEO) {
            return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO : MESSAGE_TYPE_SENT_VIDEO;
        }
        if (message.getType() == EMMessage.Type.FILE) {
            return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_FILE : MESSAGE_TYPE_SENT_FILE;
        }
        return -1;// invalid
    }

    public int getViewTypeCount() {
        return 16;
    }

    @SuppressLint("InflateParams")
    private View createViewByMessage(EMMessage message, int position) {
        switch (message.getType()) {
            case LOCATION:
                return message.direct() == EMMessage.Direct.RECEIVE ? inflater
                        .inflate(R.layout.row_received_location, null) : inflater
                        .inflate(R.layout.row_sent_location, null);
            case IMAGE:
                return message.direct() == EMMessage.Direct.RECEIVE ? inflater
                        .inflate(R.layout.row_received_picture, null) : inflater
                        .inflate(R.layout.row_sent_picture, null);

            case VOICE:
                return message.direct() == EMMessage.Direct.RECEIVE ? inflater
                        .inflate(R.layout.row_received_voice, null) : inflater
                        .inflate(R.layout.row_sent_voice, null);
            case VIDEO:
                return message.direct() == EMMessage.Direct.RECEIVE ? inflater
                        .inflate(R.layout.row_received_video, null) : inflater
                        .inflate(R.layout.row_sent_video, null);
            case FILE:
                return message.direct() == EMMessage.Direct.RECEIVE ? inflater
                        .inflate(R.layout.row_received_file, null) : inflater
                        .inflate(R.layout.row_sent_file, null);
            default:
                // 语音电话
                if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false))
                    return message.direct() == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_voice_call, null) : inflater.inflate(R.layout.row_sent_voice_call, null);
                return message.direct() == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_message, null) : inflater.inflate(R.layout.row_sent_message, null);
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final EMMessage message = getItem(position);
        ViewHolder holder;
        EMMessage.ChatType chatType = message.getChatType();
        String fromUserNick = "";
        String fromUserAvatar = "";
        EMMessage.Direct msg_dirct = message.direct();
        try {
            fromUserNick = message.getStringAttribute("userNick");
            fromUserAvatar = message.getStringAttribute("userAvatar");
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        if (message.getFrom().equals("admin")) {
            convertView = LayoutInflater.from(context).inflate(R.layout.social_chat_admin_item, null);
            TextView timestamp = (TextView) convertView.findViewById(R.id.tv_time);
            TextView tv_content = (TextView) convertView.findViewById(R.id.tv_content);
            timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
            EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
            Spannable span = SmileUtils.getSmiledText(context, txtBody.getMessage());
            // 设置内容
            tv_content.setText(span, TextView.BufferType.SPANNABLE);
            return convertView;

        } else {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = createViewByMessage(message, position);
                if (message.getType() == EMMessage.Type.LOCATION) {

                } else if (message.getType() == EMMessage.Type.IMAGE) {

                } else if (message.getType() == EMMessage.Type.VOICE) {
                    try {
                        holder.iv = ((ImageView) convertView.findViewById(R.id.iv_voice));
                        holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
                        holder.tv = (TextView) convertView.findViewById(R.id.tv_length);
                        holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                        holder.status_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                        holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                        holder.iv_read_status = (ImageView) convertView.findViewById(R.id.iv_unread_voice);
                    } catch (Exception e) {
                    }
                } else if (message.getType() == EMMessage.Type.VIDEO) {

                } else if (message.getType() == EMMessage.Type.FILE) {

                } else if (message.getType() == EMMessage.Type.TXT) {
                    try {
                        holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                        holder.status_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                        holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
                        // 这里是文字内容
                        holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                        holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                    } catch (Exception e) {
                    }
                    // 语音通话及视频通话
                    if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)
                            || message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
                        holder.iv = (ImageView) convertView.findViewById(R.id.iv_call_icon);
                        holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                    }
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // 群聊时，显示接收的消息的发送人的名称
            if (chatType == EMMessage.ChatType.GroupChat && message.direct() == EMMessage.Direct.RECEIVE)
                // demo用username代替nick
                holder.tv_userId.setText(fromUserNick);
            // 如果是发送的消息并且不是群聊消息，显示已读textView
            if (message.direct() == EMMessage.Direct.SEND && chatType != EMMessage.ChatType.GroupChat) {
                holder.tv_ack = (TextView) convertView.findViewById(R.id.tv_ack);
                holder.tv_delivered = (TextView) convertView.findViewById(R.id.tv_delivered);
                if (holder.tv_ack != null) {
                    if (message.isAcked()) {
                        if (holder.tv_delivered != null) {
                            holder.tv_delivered.setVisibility(View.INVISIBLE);
                        }
                        holder.tv_ack.setVisibility(View.VISIBLE);
                    } else {
                        holder.tv_ack.setVisibility(View.INVISIBLE);
                        // check and display msg delivered ack status
                        if (holder.tv_delivered != null) {
                            if (message.isDelivered()) {
                                holder.tv_delivered.setVisibility(View.VISIBLE);
                            } else {
                                holder.tv_delivered.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
            } else {
                // 如果是文本或者地图消息并且不是group messgae，显示的时候给对方发送已读回执
                if ((message.getType() == EMMessage.Type.TXT || message.getType() == EMMessage.Type.LOCATION)
                        && !message.isAcked() && chatType != EMMessage.ChatType.GroupChat) {
                    // 不是语音通话记录
                    if (!message.getBooleanAttribute(
                            Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                        try {
                            EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
                            // 发送已读回执
                            message.setAcked(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            switch (message.getType()) {
                // 根据消息type显示item
                case IMAGE: // 图片
                    break;
                case TXT: // 文本
                    if (message.getBooleanAttribute(
                            Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)
                            || message.getBooleanAttribute(
                            Constant.MESSAGE_ATTR_IS_VIDEO_CALL, false))
                        // 音视频通话
                        handleCallMessage(message, holder);
                    else
                        handleTextMessage(message, holder);
                    break;
                case LOCATION: // 位置
                    break;
                case VOICE: // 语音
                    handleVoiceMessage(message, holder);
                    break;
                case VIDEO: // 视频
                    break;
                case FILE: // 一般文件
                    break;
                default:
                    // not supported
            }
            if (message.direct() == EMMessage.Direct.SEND) {
                View statusView = convertView.findViewById(R.id.msg_status);
                // 重发按钮点击事件
                statusView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 显示重发消息的自定义alertdialog
                        Intent intent = new Intent(activity, KenAlertDialog.class);
                        intent.putExtra("msg", activity.getString(R.string.confirm_resend));
                        intent.putExtra("title", activity.getString(R.string.resend));
                        intent.putExtra("cancel", true);
                        intent.putExtra("position", position);
                        if (message.getType() == EMMessage.Type.TXT)
                            activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_TEXT);
                        else if (message.getType() == EMMessage.Type.VOICE)
                            activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_VOICE);
                        else if (message.getType() == EMMessage.Type.IMAGE)
                            activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_PICTURE);
                        else if (message.getType() == EMMessage.Type.LOCATION)
                            activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_LOCATION);
                        else if (message.getType() == EMMessage.Type.FILE)
                            activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_FILE);
                        else if (message.getType() == EMMessage.Type.VIDEO)
                            activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_VIDEO);
                    }
                });
            } else {
                // 长按头像，移入黑名单
                holder.head_iv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return true;
                    }
                });
            }
            TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);
            if (position == 0) {
                timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
                timestamp.setVisibility(View.VISIBLE);
            } else {
                // 两条消息时间离得如果稍长，显示时间
                if (DateUtils.isCloseEnough(message.getMsgTime(), msgs.get(position - 1).getMsgTime())) {
                    timestamp.setVisibility(View.GONE);
                } else {
                    timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
                    timestamp.setVisibility(View.VISIBLE);
                }
            }
            // 暂时设置头像。。。实际网络获取
            if (msg_dirct == EMMessage.Direct.RECEIVE) {
                holder.head_iv.setImageResource(R.mipmap.ic_launcher);
            } else {
                holder.head_iv.setImageResource(R.mipmap.ic_launcher);
            }
        }
        return convertView;
    }

    /**
     * 文本消息
     *
     * @param message
     * @param holder
     */
    private void handleTextMessage(EMMessage message, ViewHolder holder) {
        EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
        Spannable span = SmileUtils.getSmiledText(context, txtBody.getMessage());
        // 设置内容
        holder.tv.setText(span, TextView.BufferType.SPANNABLE);
        if (message.direct() == EMMessage.Direct.SEND) {
            switch (message.status()) {
                case SUCCESS: // 发送成功
                    holder.pb.setVisibility(View.GONE);
                    holder.status_iv.setVisibility(View.GONE);
                    break;
                case FAIL: // 发送失败
                    holder.pb.setVisibility(View.GONE);
                    holder.status_iv.setVisibility(View.VISIBLE);
                    break;
                case INPROGRESS: // 发送中
                    holder.pb.setVisibility(View.VISIBLE);
                    holder.status_iv.setVisibility(View.GONE);
                    break;
                default:
                    // 发送消息
                    sendMsgInBackground(message, holder);
            }
        }
    }

    /**
     * 语音消息
     *
     * @param message
     * @param holder
     */
    private void handleVoiceMessage(final EMMessage message, final ViewHolder holder) {
        EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) message.getBody();
        holder.tv.setText(voiceBody.getLength() + "\"");
        holder.iv.setOnClickListener(new VoicePlayClickListener(message, holder.iv, holder.iv_read_status, this, activity, username));
        if (((ChatActivity) activity).playMsgId != null && ((ChatActivity) activity).playMsgId.equals(message.getMsgId()) && VoicePlayClickListener.isPlaying) {
            AnimationDrawable voiceAnimation;
            if (message.direct() == EMMessage.Direct.RECEIVE) {
                holder.iv.setImageResource(R.anim.voice_from_icon);
            } else {
                holder.iv.setImageResource(R.anim.voice_to_icon);
            }
            voiceAnimation = (AnimationDrawable) holder.iv.getDrawable();
            voiceAnimation.start();
        } else {
            if (message.direct() == EMMessage.Direct.RECEIVE) {
                holder.iv.setImageResource(R.mipmap.chatfrom_voice_playing);
            } else {
                holder.iv.setImageResource(R.mipmap.chatto_voice_playing);
            }
        }
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            if (message.isListened()) {
                // 隐藏语音未听标志
                holder.iv_read_status.setVisibility(View.INVISIBLE);
            } else {
                holder.iv_read_status.setVisibility(View.VISIBLE);
            }
            System.err.println("it is receive msg");
            if (message.status() == EMMessage.Status.INPROGRESS) {
                holder.pb.setVisibility(View.VISIBLE);
                System.err.println("!!!! back receive");
                message.setMessageStatusCallback(new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.INVISIBLE);
                                notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onProgress(int progress, String status) {
                    }

                    @Override
                    public void onError(int code, String message) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                });
            } else {
                holder.pb.setVisibility(View.INVISIBLE);
            }
            return;
        }
        // until here, deal with send voice msg
        switch (message.status()) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.status_iv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.status_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                holder.pb.setVisibility(View.VISIBLE);
                holder.status_iv.setVisibility(View.GONE);
                break;
            default:
                sendMsgInBackground(message, holder);
        }
    }

    /**
     * 音视频通话记录
     *
     * @param message
     * @param holder
     */
    private void handleCallMessage(EMMessage message, ViewHolder holder) {
        EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
        holder.tv.setText(txtBody.getMessage());
    }

    /**
     * 发送消息
     *
     * @param message
     * @param holder
     * @param
     */
    public void sendMsgInBackground(final EMMessage message, final ViewHolder holder) {
        holder.status_iv.setVisibility(View.GONE);
        holder.pb.setVisibility(View.VISIBLE);
        // 调用sdk发送异步发送方法
        EMClient.getInstance().chatManager().sendMessage(message);
        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                updateSentView(message, holder);
            }

            @Override
            public void onError(int i, String s) {
                updateSentView(message, holder);
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    /**
     * 更新ui上消息发送状态
     *
     * @param message
     * @param holder
     */
    private void updateSentView(final EMMessage message, final ViewHolder holder) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // send success
                if (message.getType() == EMMessage.Type.VIDEO) {
                    holder.tv.setVisibility(View.GONE);
                }
                if (message.status() == EMMessage.Status.SUCCESS) {
                    holder.pb.setVisibility(View.GONE);
                } else if (message.status() == EMMessage.Status.FAIL) {
                    Toast.makeText(context, context.getString(R.string.send_fail) + context.getString(R.string.connect_failuer_toast), Toast.LENGTH_LONG).show();
                }
                notifyDataSetChanged();
            }
        });
    }

    public static class ViewHolder {
        ImageView iv;
        TextView tv;
        ProgressBar pb;
        ImageView status_iv;
        ImageView head_iv;
        TextView tv_userId;
        ImageView playBtn;
        TextView timeLength;
        TextView size;
        LinearLayout container_status_btn;
        LinearLayout ll_container;
        ImageView iv_read_status;
        // 显示已读回执状态
        TextView tv_ack;
        // 显示送达回执状态
        TextView tv_delivered;
        TextView tv_file_name;
        TextView tv_file_size;
        TextView tv_file_download_state;
    }
}
