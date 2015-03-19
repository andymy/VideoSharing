package com.fju.videosharing.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoFrameActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.gc();
		Intent i = getIntent();
		Bundle extras = i.getExtras();
		String filename = extras.getString("Video_File_Name");
		VideoView mVideoView = new VideoView(getApplicationContext());
		setContentView(mVideoView);
		// Set the path of Video or URI
		// mVideoView.setVideoPath(filename);
		mVideoView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mVideoView.setVideoURI(Uri.parse(filename));
		mVideoView.setMediaController(new MediaController(this));
		mVideoView.requestFocus();
		// mVideoView.seekTo(10);
		mVideoView.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.gc();
	}
}
