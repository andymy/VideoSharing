package com.fju.videosharing.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.fju.videosharing.R;
import com.fju.videosharing.util.APIConstant;
import com.fju.videosharing.util.GlobalVariable;
import com.fju.videosharing.util.HttpUtil;
import com.fju.videosharing.view.VideoList;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class VideoListActivity extends Activity {

	private static final int CODE_ACTION_SEARCH_VIDEO = 102;

	private String userName;
	private VideoList list;
	private ProgressBar listProgress;
	private int objNumber = 0;
	private ImageLoader imageLoader;
	private Spinner spinner;
	private ArrayAdapter<String> adapter;
	private EditText etSearch;
	private boolean isInit = true;
	private boolean isSearching = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_list_layout);
		list = (VideoList) findViewById(R.id.list);
		list.setLayout(R.layout.list_single);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent();
				Log.i("", "URI= " + list.getVideoURI(position));
				intent.setClass(VideoListActivity.this, VideoFrameActivity.class);
				intent.putExtra("Video_File_Name", list.getVideoURI(position));
				startActivity(intent);
			}
		});

		listProgress = (ProgressBar) findViewById(R.id.progressBar_list);
		imageLoader = ImageLoader.getInstance();
		DisplayImageOptions options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.video_icon)
				.showImageOnFail(R.drawable.video_icon).build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(options).build();
		imageLoader.init(config);

		etSearch = (EditText) findViewById(R.id.editText_search);
		spinner = (Spinner) findViewById(R.id.author_spinner);
		// 下拉選單內容
		final String[] orderState = getResources().getStringArray(R.array.author_array);
		// 建立一個ArrayAdapter物件，並放置下拉選單的內容
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, orderState);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // 設定下拉選單的樣式
		spinner.setAdapter(adapter);
		// 設定項目被選取之後的動作
		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				if (!isInit && !isSearching) {
					switch (position) {
					case 0:
						searchAuthor(userName); // 自己
						break;
					case 1:
						searchAuthor("*.*ALL"); // 所有
						break;
					case 2:
						break;
					default:
						break;
					}
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
		Button btnSearch = (Button) findViewById(R.id.button_search);
		btnSearch.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isSearching) {
					String searchName = etSearch.getText().toString();
					if (!searchName.equalsIgnoreCase("")) {
						spinner.setSelection(2);
						LoginActivity.hideSoftKeyboard(VideoListActivity.this);
						searchAuthor(searchName);
					}
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		userName = ((GlobalVariable) getApplication()).getLoginUserName();
		Log.i("", "userName" + userName);
		isInit = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		imageLoader.stop();
	}

	@Override
	protected void onDestroy() {
		System.gc();
		super.onDestroy();
		imageLoader.stop();
		list.clear();
	}

	private void searchAuthor(String authorName) {
		isSearching = true;
		spinner.setClickable(false);
		Log.i("", "authorName = " + authorName);
		list.clear();
		list.show();
		listProgress.setVisibility(View.VISIBLE);
		JSONObject jsonObjSend = new JSONObject();
		try {
			jsonObjSend.put("author", authorName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		APIJsonThread jsonThread = new APIJsonThread(GlobalVariable.BASE + APIConstant.SearchVideo, jsonObjSend, CODE_ACTION_SEARCH_VIDEO);
		jsonThread.start();
	}

	public class APIJsonThread extends Thread {

		private String url;
		private Object json;
		private int action;

		public APIJsonThread(String url, Object json, int action) {
			this.url = url;
			this.json = json;
			this.action = action;
		}

		@Override
		public void run() {
			// Send the HttpPostRequest and receive a JSONObject in return
			Log.i("", "json= " + json);
			String result = HttpUtil.connRemote(json, url);
			Message msg = new Message();
			msg.what = action;
			msg.obj = result;
			handler.sendMessage(msg);
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		JSONObject result = null;
		boolean flag = false; // 请求是否成功 success|error

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String msgResult = (String) msg.obj;
			try {
				result = new JSONObject(URLDecoder.decode(msgResult.toString(), "utf-8"));
				String status = result.getString(HttpUtil.STATUS);
				String message = result.getString(HttpUtil.MESSAGE);
				if (status.equalsIgnoreCase(HttpUtil.SUCCESS)) {
					flag = true;
				} else {
					flag = false;
					listProgress.setVisibility(View.GONE);
					showDialog(message); // 連線失敗
				}
				if (flag) {
					switch (msg.what) {
					case CODE_ACTION_SEARCH_VIDEO:
						list.clear();
						JSONArray jsonArray = new JSONArray(result.getString("videos"));
						APIImageThread uploadThread = new APIImageThread(jsonArray);
						uploadThread.start();
						break;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	};

	public class APIImageThread extends Thread {

		private JSONArray json;

		public APIImageThread(JSONArray json) {
			this.json = json;
		}

		@Override
		public void run() {
			try {
				objNumber = json.length();
				if (objNumber > 0) {
					JSONObject object = null;
					for (int n = 0; n < objNumber; n++) {
						object = json.getJSONObject(n);
						String image = object.getString("image").toString().replaceAll("\"", "").trim();
						Bitmap bitmap = imageLoader.loadImageSync(image);
						object.put("image", bitmap);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Message msg = new Message();
			msg.obj = json;
			imageHandler.sendMessage(msg);
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler imageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				JSONArray jsonArray = (JSONArray) msg.obj;
				Log.i("", "jsonArray= " + jsonArray.toString(2));
				objNumber = jsonArray.length();
				if (objNumber > 0) {
					for (int n = 0; n < objNumber; n++) {
						JSONObject object = jsonArray.getJSONObject(n);
						String creat_time = object.getString("creat_time");
						String title = object.getString("title");
						String author = object.getString("author");
						String describe = object.getString("describe");
						String video = object.getString("video").toString().replaceAll("\"", "").trim();
						Bitmap bitmap = (Bitmap) object.get("image");
						list.addList(video, creat_time, title, author, describe, bitmap);
						list.show();
					}
				}
				listProgress.setVisibility(View.GONE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			isSearching = false;
			spinner.setClickable(true);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.activity01, menu);
		return true;
	}

	private void showDialog(final String msg) {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(msg).setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// Intent intent = new Intent();
					// intent.setClass(VideoListActivity.this,
					// MainActivity.class);
					// startActivity(intent);
					// VideoListActivity.this.finish();
					isSearching = false;
					spinner.setClickable(true);
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
