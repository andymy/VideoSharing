package com.fju.videosharing.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fju.videosharing.R;

public class Fragment02 extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("Fragment02", "onCreateView");
		View v = inflater
				.inflate(R.layout.fragment_layout_02, container, false);
		TextView text = (TextView) v.findViewById(R.id.text_view2);
		text.setText(" Fragment 2 !");
		return v;
	}
}