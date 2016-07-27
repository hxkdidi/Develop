package com.kenos.kenos.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.kenos.kenos.R;
import com.kenos.kenos.base.BaseActivity;
import com.kenos.kenos.model.beauty.Img;
import com.kenos.kenos.net.BeautyRetrofit;

import java.util.List;

import rx.Subscriber;

/**
 * User: hxk(huangxikang@520dyw.cn)
 * Date: 2016-07-27
 * Time: 17:19
 * Description:
 */
public class TestActivity extends BaseActivity {

    private Subscriber<List<Img>> subscriber;
    private int pn = 20;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
    }

    private void initView() {
        textView = (TextView) findViewById(R.id.textView);
        subscriber = new Subscriber<List<Img>>() {

            @Override
            public void onCompleted() {
                Toast.makeText(TestActivity.this, "获取成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(TestActivity.this, "服务器繁忙", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(List<Img> imgs) {
                textView.setText(imgs.toString());
            }
        };
        BeautyRetrofit.getSingleton().getBeautyPageList(subscriber, pn);
    }
}
