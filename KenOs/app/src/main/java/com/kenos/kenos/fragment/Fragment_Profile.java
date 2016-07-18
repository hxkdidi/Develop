package com.kenos.kenos.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kenos.kenos.R;

/**
 * 个人中心界面
 * 
 * @author allenjuns@yahoo.com
 *
 */
public class Fragment_Profile extends Fragment {
	private Activity ctx;
	private View layout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (layout == null) {
			ctx = this.getActivity();
			layout = ctx.getLayoutInflater().inflate(R.layout.fragment_profile,
					null);
			initView();
		} else {
			ViewGroup parent = (ViewGroup) layout.getParent();
			if (parent != null) {
				parent.removeView(layout);
			}
		}
		return layout;
	}

	private void initView() {
	}
}
