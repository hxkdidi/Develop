package com.kenos.kenos.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.kenos.kenos.R;
import com.kenos.kenos.adapter.NewFriendsMsgAdapter;
import com.kenos.kenos.base.BaseActivity;
import com.kenos.kenos.db.InviteMessage;
import com.kenos.kenos.db.InviteMessageDao;

import java.util.List;


/**
 * 申请与通知
 */
public class NewFriendsMsgActivity extends BaseActivity {
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friends_msg);
        listView = (ListView) findViewById(R.id.list);
        InviteMessageDao dao = new InviteMessageDao(this);
        List<InviteMessage> msgs = dao.getMessagesList();
        NewFriendsMsgAdapter adapter = new NewFriendsMsgAdapter(this, 1, msgs);
        listView.setAdapter(adapter);
        dao.saveUnreadMessageCount(0);
    }

    public void back(View view) {
        finish();
    }
}
