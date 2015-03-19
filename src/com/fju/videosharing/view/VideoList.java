package com.fju.videosharing.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

import com.fju.videosharing.R;

public class VideoList extends ListView {

	private Context mCtx;
	private String[] mFrom; // 是Map的名稱，可自行定義
	private int[] mTo;
	private ArrayList<HashMap<String, Object>> mFillMaps = null;
	private SimpleAdapter mAdapter = null;

	public VideoList(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCtx = context;
	}

	public void setLayout(int listLayout) {
		mFillMaps = new ArrayList<HashMap<String, Object>>();
		mFrom = new String[] { "uri", "date", "title", "author", "about", "preview" };
		mTo = new int[] { 0, R.id.upload_video_date, R.id.upload_video_title, R.id.upload_video_author, R.id.upload_video_about,
				R.id.upload_video_preview };
		mAdapter = new SimpleAdapter(mCtx, mFillMaps, listLayout, mFrom, mTo);
		mAdapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				if (view instanceof ImageView && data instanceof Bitmap) {
					ImageView iv = (ImageView) view;
					iv.setImageBitmap((Bitmap) data);
					return true;
				} else
					return false;
			}
		});
		setAdapter(mAdapter);
		setClickable(true);
	}

	public int getSeq(int seqAt, int pos) {
		HashMap<String, Object> obj = mFillMaps.get(pos);
		return Integer.valueOf(obj.get(mFrom[seqAt]).toString());
	}

	public String getVideoURI(int pos) {
		HashMap<String, Object> obj = mFillMaps.get(pos);
		return obj.get("uri").toString();
	}

	public Object getVideoInfo(String data, int pos) {
		HashMap<String, Object> obj = mFillMaps.get(pos);
		return obj.get(data);
	}

	public void setVideoPreview(Bitmap data, int pos) {
		HashMap<String, Object> obj = mFillMaps.get(pos);
		obj.put("preview", data);
		mFillMaps.set(pos, obj);
		mAdapter.notifyDataSetChanged();
	}

	public void clear() {
		mFillMaps.clear();
	}

	public void show() {
		mAdapter.notifyDataSetChanged();
	}

	public void addList(Object... args) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		for (int i = 0; i < mFrom.length; i++) {
			map.put(mFrom[i], args[i]);
		}
		mFillMaps.add(0, map); // 新增到最前頭
	}

	public void remove(int s) {
		mFillMaps.remove(s);
	}

	public void addAllList(List<HashMap<String, Object>> collection) {
		mFillMaps = new ArrayList<HashMap<String, Object>>();
		mFillMaps.addAll(collection);
		mAdapter.notifyDataSetChanged();
	}

	public List<?> getAllItems() {
		if (mFillMaps.size() > 0)
			return mFillMaps.subList(0, mFillMaps.size() - 1);
		return mFillMaps;
	}

	public void setViewBinder() {
		mAdapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				if (view instanceof ImageView && data instanceof Bitmap) {
					ImageView iv = (ImageView) view;
					iv.setImageBitmap((Bitmap) data);
					return true;
				} else
					return false;
			}
		});
	}

}
