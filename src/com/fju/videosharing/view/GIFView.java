package com.fju.videosharing.view;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

public class GIFView extends View {

	Movie movie;
	long moviestart;

	public GIFView(Context context) throws IOException {
		super(context);
	}

	public GIFView(Context context, AttributeSet attrs) throws IOException {
		super(context, attrs);
	}

	public GIFView(Context context, AttributeSet attrs, int defStyle) throws IOException {
		super(context, attrs, defStyle);
	}

	public void loadGIFResource(Context context, String fileName) throws IOException {
		// turn off hardware acceleration
		// this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		InputStream is = getResources().getAssets().open(fileName);
		movie = Movie.decodeStream(is);
	}

	public void loadGIFAsset(Context context, String filename) {
		InputStream is;
		try {
			is = context.getResources().getAssets().open(filename);
			movie = Movie.decodeStream(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		long now = android.os.SystemClock.uptimeMillis();
		if (moviestart == 0) { // first time
			moviestart = now;
		}
		if (movie != null) {

			int dur = movie.duration();
			if (dur == 0) {
				dur = 1000;
			}
			int relTime = (int) ((now - moviestart) % dur);
			movie.setTime(relTime);
			movie.draw(canvas, 0, 0);
			invalidate();
		}
	}
}