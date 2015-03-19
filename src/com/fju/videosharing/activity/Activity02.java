package com.fju.videosharing.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fju.videosharing.R;
import com.fju.videosharing.animation.CollapseAnimation;
import com.fju.videosharing.animation.ExpandAnimation;
import com.fju.videosharing.animation.FunctinoCloseAnimation;
import com.fju.videosharing.animation.FunctionShowAnimation;
import com.fju.videosharing.fragment.Fragment01;

public class Activity02 extends FragmentActivity {
	/** Called when the activity is first created. */
	private Button fragmentBtn01, fragmentBtn02;
	private ImageView showFunctionButton;
	private ImageView showMenuButton;
	private LinearLayout menuPanel;
	private LinearLayout slidingPanel;
	private LinearLayout slidingContext;
	private int functionPanelHeight;
	private int menuPanelWidth;

	private boolean isMenuExpanded = true;
	private boolean isFunctionHide = true;
	private float slideWeight = 0.95f;
	private DisplayMetrics metrics;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layout_02);
		Button button = (Button) findViewById(R.id.button02);

		button.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(Activity02.this, MainActivity.class);
				startActivity(intent);
				Activity02.this.finish();
			}
		});

		fragmentBtn01 = (Button) findViewById(R.id.fragment1_button);
		fragmentBtn02 = (Button) findViewById(R.id.fragment2_button);
		final Fragment01 f1 = new Fragment01();
		// final Fragment02 f2 = new Fragment02();
		changeFragment(f1);
		fragmentBtn01.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showFunctionView(false);
				// changeFragment(f1);
			}
		});
		fragmentBtn02.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showFunctionView(true);
				// changeFragment(f2);
			}
		});

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		menuPanelWidth = (int) ((metrics.widthPixels) * 0.75);

		// Initialize
		menuPanel = (LinearLayout) findViewById(R.id.menuViewPanel);
		menuPanel.setVisibility(View.GONE);
		showMenuButton = (ImageView) findViewById(R.id.menuViewButton);
		showMenuButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isMenuExpanded) {
					isMenuExpanded = true;
					// Expand
					new ExpandAnimation(menuPanel, menuPanelWidth, Animation.RELATIVE_TO_SELF, 0.0f,
							Animation.RELATIVE_TO_SELF, -1.0f, 0, 0.0f, 0, 0.0f);
				} else {
					isMenuExpanded = false;
					// Collapse
					new CollapseAnimation(menuPanel, menuPanelWidth, TranslateAnimation.RELATIVE_TO_SELF,
							-1.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f, 0, 0.0f, 0, 0.0f);
				}
			}
		});

		slidingPanel = (LinearLayout) findViewById(R.id.slidingPanel);
		slidingContext = (LinearLayout) findViewById(R.id.slide_context);
		final ViewTreeObserver observer = slidingPanel.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Log.d("", "slidingPanel.getHeight() = " + slidingPanel.getHeight());
				functionPanelHeight = (int) (slidingPanel.getHeight() * slideWeight);
			}
		});
		showFunctionButton = (ImageView) findViewById(R.id.functionViewButton);
		showFunctionButton.setBackgroundColor(R.drawable.button_bg_ocean);
		showFunctionButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (isFunctionHide) {
					showFunctionView(true);
				} else {
					showFunctionView(false);
				}
			}
		});
	}

	private void showFunctionView(boolean isShow) {
		if (isShow) {
			if (isFunctionHide == true) {
				isFunctionHide = false;
				// Show
				new FunctionShowAnimation(slidingPanel, slidingContext, showFunctionButton,
						functionPanelHeight, 0, 0.0f, 0, 0.0f, TranslateAnimation.RELATIVE_TO_SELF,
						slideWeight, TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
			}
		} else {
			if (isFunctionHide == false) {
				isFunctionHide = true;
				// Hide
				new FunctinoCloseAnimation(slidingPanel, slidingContext, showFunctionButton,
						functionPanelHeight, 0, 0.0f, 0, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, slideWeight);
			}
		}
	}

	private void changeFragment(Fragment f) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		int stackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
		if (stackEntryCount == 0) {
			transaction.replace(R.id.fragment_container, f, f + "");
			transaction.addToBackStack(f.getTag());
			Log.d("1", "f.getTag() = " + f.getTag());
			transaction.commit();
		} else {
			for (int i = 0; i < stackEntryCount; i++) {
				String currentFrg = getSupportFragmentManager().getBackStackEntryAt(i).getName();
				if (currentFrg.equals(f.getTag())) {
					f = (Fragment) getSupportFragmentManager().findFragmentByTag(currentFrg);
					transaction.replace(R.id.fragment_container, f);
					Log.d("2", "f.getTag() = " + f.getTag());
					transaction.commit();
					return;
				}
				continue;
			}
			transaction.replace(R.id.fragment_container, f, f + "");
			transaction.addToBackStack(f.getTag());
			Log.d("3", "f.getTag() = " + f.getTag());
			transaction.commit();
		}
	}
}