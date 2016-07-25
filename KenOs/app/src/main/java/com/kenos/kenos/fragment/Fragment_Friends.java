package com.kenos.kenos.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.kenos.kenos.R;
import com.kenos.kenos.activity.AddContactActivity;
import com.kenos.kenos.activity.ChatActivity;
import com.kenos.kenos.activity.NewFriendsMsgActivity;
import com.kenos.kenos.app.KenApplication;
import com.kenos.kenos.base.BaseFragment;
import com.kenos.kenos.db.EaseUser;
import com.kenos.kenos.utils.EaseCommonUtils;
import com.kenos.kenos.utils.PingYinUtil;
import com.kenos.kenos.view.SideBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 通讯录界面
 *
 * @author allenjuns@yahoo.com
 */
public class Fragment_Friends extends BaseFragment {
    private Activity ctx;
    private View layout, layout_head;
    private Map<String, EaseUser> contactsMap;
    private ListView lvContact;
    private SideBar indexBar;
    private TextView mDialogText;
    private WindowManager mWindowManager;
    protected List<EaseUser> contactList = new ArrayList<EaseUser>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (layout == null) {
            ctx = this.getActivity();
            layout = ctx.getLayoutInflater().inflate(R.layout.fragment_friends, null);
            initView(layout);
        } else {
            ViewGroup parent = (ViewGroup) layout.getParent();
            if (parent != null) {
                parent.removeView(layout);
            }
        }
        return layout;
    }

    private void initView(View view) {
        mWindowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        lvContact = (ListView) layout.findViewById(R.id.lvContact);
        indexBar = (SideBar) layout.findViewById(R.id.sideBar);
        indexBar.setListView(lvContact);
        mDialogText = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.list_position, null);
        mDialogText.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mWindowManager.addView(mDialogText, lp);
        indexBar.setTextView(mDialogText);
        layout_head = ctx.getLayoutInflater().inflate(R.layout.layout_head_friend, null);
        lvContact.addHeaderView(layout_head);
        view.findViewById(R.id.layout_addfriend).setOnClickListener(this);
        view.findViewById(R.id.layout_group).setOnClickListener(this);
        view.findViewById(R.id.layout_public).setOnClickListener(this);
        getContactList();
        lvContact.setAdapter(new ContactAdapter(ctx, contactList));
        lvContact.setOnItemClickListener(this);
    }

    /**
     * 联系人的适配器
     */
    static class ContactAdapter extends BaseAdapter implements SectionIndexer {
        private Context mContext;
        private List<EaseUser> users;
        private LayoutInflater inflater;

        public ContactAdapter(Context mContext, List<EaseUser> users) {
            this.mContext = mContext;
            this.users = users;
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final String nickName = users.get(position).getNick();
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.contact_item, null);
                viewHolder = new ViewHolder();
                viewHolder.tvCatalog = (TextView) convertView.findViewById(R.id.contactitem_catalog);
                viewHolder.ivAvatar = (ImageView) convertView.findViewById(R.id.contactitem_avatar_iv);
                viewHolder.tvNick = (TextView) convertView.findViewById(R.id.contactitem_nick);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String catalog = PingYinUtil.converterToFirstSpell(nickName).substring(0, 1);
            if (position == 0) {
                viewHolder.tvCatalog.setVisibility(View.VISIBLE);
                viewHolder.tvCatalog.setText(catalog);
            } else {
                String lastCatalog = PingYinUtil.converterToFirstSpell(users.get(position - 1).getNick()).substring(0, 1);
                if (catalog.equals(lastCatalog)) {
                    viewHolder.tvCatalog.setVisibility(View.GONE);
                } else {
                    viewHolder.tvCatalog.setVisibility(View.VISIBLE);
                    viewHolder.tvCatalog.setText(catalog);
                }
            }
            viewHolder.ivAvatar.setImageResource(R.mipmap.head);
            viewHolder.tvNick.setText(nickName);
            return convertView;
        }

        static class ViewHolder {
            TextView tvCatalog;// 目录
            ImageView ivAvatar;// 头像
            TextView tvNick;// 昵称
        }

        @Override
        public int getPositionForSection(int section) {
            for (int i = 0; i < users.size(); i++) {
                String l = PingYinUtil.converterToFirstSpell(users.get(i).getNick()).substring(0, 1);
                char firstChar = l.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }

        @Override
        public Object[] getSections() {
            return null;
        }
    }

    /**
     * 获取联系人列表，并过滤掉黑名单和排序
     */
    protected void getContactList() {
        contactList.clear();
        // 获取联系人列表
        contactsMap = KenApplication.getInstance().getContactList();
        if (contactsMap == null) {
            return;
        }
        synchronized (this.contactsMap) {
            Iterator<Map.Entry<String, EaseUser>> iterator = contactsMap.entrySet().iterator();
            List<String> blackList = EMClient.getInstance().contactManager().getBlackListUsernames();
            while (iterator.hasNext()) {
                Map.Entry<String, EaseUser> entry = iterator.next();
                // 兼容以前的通讯录里的已有的数据显示，加上此判断，如果是新集成的可以去掉此判断
                if (!entry.getKey().equals("item_new_friends") && !entry.getKey().equals("item_groups")
                        && !entry.getKey().equals("item_chatroom") && !entry.getKey().equals("item_robots")) {
                    if (!blackList.contains(entry.getKey())) {
                        // 不显示黑名单中的用户
                        EaseUser user = entry.getValue();
                        EaseCommonUtils.setUserInitialLetter(user);
                        contactList.add(user);
                    }
                }
            }
        }

        // 排序
        Collections.sort(contactList, new Comparator<EaseUser>() {
            @Override
            public int compare(EaseUser lhs, EaseUser rhs) {
                if (lhs.getInitialLetter().equals(rhs.getInitialLetter())) {
                    return lhs.getNick().compareTo(rhs.getNick());
                } else {
                    if ("#".equals(lhs.getInitialLetter())) {
                        return 1;
                    } else if ("#".equals(rhs.getInitialLetter())) {
                        return -1;
                    }
                    return lhs.getInitialLetter().compareTo(rhs.getInitialLetter());
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_addfriend:
                startActivity(new Intent(ctx, AddContactActivity.class));
                break;
            case R.id.layout_group:
                break;
            case R.id.layout_public:
                startActivity(new Intent(ctx, NewFriendsMsgActivity.class));
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);
        EaseUser easeUser = contactList.get(position - 1);
        // 进入聊天页面
        Intent intent = new Intent(ctx, ChatActivity.class);
        intent.putExtra("userName", easeUser.getUsername());
        intent.putExtra("userAvatar", easeUser.getAvatar());
        intent.putExtra("userNick", easeUser.getNick());
        startActivity(intent);
    }
}
