/*  Created by Edward Akoto on 12/31/12.
 *  Email akotoe@aua.ac.ke
 * 	Free for modification and distribution
 */

package com.fju.videosharing.animation;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fju.videosharing.R;

public class FunctionShowAnimation extends TranslateAnimation implements TranslateAnimation.AnimationListener {

	private LinearLayout slidingLayout;
	private LinearLayout slidingContext;
	private ImageView slidingImage;
	int panelWidth;

	public FunctionShowAnimation(LinearLayout layout, LinearLayout context, ImageView image, int width, int fromXType, float fromXValue, int toXType,
			float toXValue, int fromYType, float fromYValue, int toYType, float toYValue) {

		super(fromXType, fromXValue, toXType, toXValue, fromYType, fromYValue, toYType, toYValue);

		// Initialize
		slidingLayout = layout;
		slidingContext = context;
		slidingImage = image;
		panelWidth = width;
		setDuration(500);
		setFillAfter(false);
		setInterpolator(new AccelerateDecelerateInterpolator());
		setAnimationListener(this);

		// Clear left and right margins
		LayoutParams params = (LayoutParams) slidingLayout.getLayoutParams();
		params.topMargin = 0;
		params.bottomMargin = 0;
		slidingContext.setVisibility(View.VISIBLE);
		slidingContext.requestLayout();
		slidingLayout.setLayoutParams(params);
		slidingLayout.requestLayout();
		slidingLayout.startAnimation(this);
	}

	public void onAnimationEnd(Animation animation) {
		slidingImage.setBackgroundColor(R.drawable.button_bg_green);
		slidingImage.setImageResource(R.drawable.icon_ios7_arrow_down);
	}

	public void onAnimationRepeat(Animation animation) {

	}

	public void onAnimationStart(Animation animation) {

	}
}
