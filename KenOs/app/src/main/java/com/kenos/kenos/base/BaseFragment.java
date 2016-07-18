package com.kenos.kenos.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;

/**
 * User: hxk(huangxikang@520dyw.cn)
 * Date: 2016-07-18
 * Time: 11:42
 * Description:
 */
public class BaseFragment extends Fragment implements View.OnClickListener ,AdapterView.OnItemClickListener{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
