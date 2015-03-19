package com.fju.videosharing.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.fju.videosharing.R;

public class Fragment01 extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("Fragment01", "onCreateView");
		View v = inflater
				.inflate(R.layout.fragment_layout_01, container, false);
		TextView text = (TextView) v.findViewById(R.id.text_view);
		text.setText(" Fragment 1 !");

		CheckBox cbBtn = (CheckBox) v.findViewById(R.id.checkBox1);
		cbBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked)
					Toast.makeText(buttonView.getContext(),
							"RadioButton checked", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(buttonView.getContext(),
							"RadioButton Not checked", Toast.LENGTH_SHORT)
							.show();
			}
		});
		return v;
	}
}