/*  Created by Edward Akoto on 12/31/12.
 *  Email akotoe@aua.ac.ke
 * 	Free for modification and distribution
 */

package com.fju.videosharing.animation;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fju.videosharing.R;

public class FunctinoCloseAnimation extends TranslateAnimation implements Animation.AnimationListener {

	private LinearLayout slidingLayout;
	private LinearLayout slidingContext;
	private ImageView slidingImage;
	int panelHeight;

	public FunctinoCloseAnimation(LinearLayout layout, LinearLayout context, ImageView image, int height, int fromXType, float fromXValue,
			int toXType, float toXValue, int fromYType, float fromYValue, int toYType, float toYValue) {

		super(fromXType, fromXValue, toXType, toXValue, fromYType, fromYValue, toYType, toYValue);

		// Initialize
		slidingLayout = layout;
		slidingContext = context;
		slidingImage = image;
		panelHeight = height;
		setDuration(350);
		setFillAfter(false);
		setInterpolator(new AccelerateDecelerateInterpolator());
		setAnimationListener(this);
		slidingLayout.startAnimation(this);
		slidingImage.setVisibility(View.GONE);
	}

	public void onAnimationEnd(Animation arg0) {
		Log.d("", "onAnimationEnd" + panelHeight);
		// Create margin and align left
		LayoutParams params = (LayoutParams) slidingLayout.getLayoutParams();
		params.topMargin = panelHeight;
		params.gravity = Gravity.TOP;
		slidingContext.setVisibility(View.GONE);
		slidingImage.setBackgroundColor(R.drawable.button_bg_green);
		slidingImage.setImageResource(R.drawable.icon_ios7_arrow_up);
		slidingImage.setVisibility(View.VISIBLE);
		slidingContext.requestLayout();
		slidingLayout.clearAnimation();
		slidingLayout.setLayoutParams(params);
		slidingLayout.requestLayout();
	}

	public void onAnimationRepeat(Animation arg0) {

	}

	public void onAnimationStart(Animation arg0) {

	}
}
