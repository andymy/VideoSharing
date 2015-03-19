package com.fju.videosharing.activity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fju.videosharing.R;
import com.fju.videosharing.util.APIConstant;
import com.fju.videosharing.util.GlobalVariable;
import com.fju.videosharing.util.HttpUtil;

public class MainActivity extends Activity {
	protected static final int REQUEST_VIDEO_CAPTURE = 1;
	protected static final int REQUEST_VIDEO_SELECT = 2;

	private static final int CODE_ACTION_UPLOAD_INFO = 100;
	private static final int CODE_ACTION_UPLOAD_VIDEO = 101;

	private String currentDateTimeString;
	private String videoPath = null;
	private TextView tVideoPath;
	private TextView tVideoDate;
	private ImageView videoPreview;
	private EditText videoTitle;
	private EditText videoContext;
	private ProgressDialog progressDialog = null;
	private String extensionName = ".mp4";
	private static String userName;
	private Handler backHandler = new Handler();
	private final Runnable backRunnable = new Runnable() {
		@Override
		public void run() {
			doubleBackToExitPressedOnce = false;
		}
	};
	private boolean doubleBackToExitPressedOnce = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		long memory = Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();
		Log.d("MainActivity", "Memory: " + memory / 1024f);
		setContentView(R.layout.activity_main_layout_01);

		Button changeBtn = (Button) findViewById(R.id.button_nextPage);
		Button fileBtn = (Button) findViewById(R.id.button_file);
		Button videoBtn = (Button) findViewById(R.id.button_video);
		Button listBtn = (Button) findViewById(R.id.button_list);
		Button sharingBtn = (Button) findViewById(R.id.button_sharing);

		currentDateTimeString = DateFormat.getDateTimeInstance().format(
				new Date());
		tVideoDate = (TextView) findViewById(R.id.textView_date);
		tVideoDate.setText(currentDateTimeString);
		tVideoPath = (TextView) findViewById(R.id.textView_video_path);
		videoTitle = (EditText) findViewById(R.id.editText_video_title);
		videoContext = (EditText) findViewById(R.id.editText_context);
		videoPreview = (ImageView) findViewById(R.id.imageView_preview);
		videoPreview.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (videoPath != null) {
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, VideoFrameActivity.class);
					intent.putExtra("Video_File_Name", videoPath);
					startActivity(intent);
				} else {
					dispatchTakeVideoIntent();
				}
			}
		});

		videoPreview.setOnLongClickListener(new Button.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				selectVideoFromFile();
				return false;
			}
		});

		changeBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, Activity02.class);
				startActivity(intent);
				// Activity01.this.finish();
			}
		});

		sharingBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (videoPath != null) {
					uploadFile();
				} else {
					showDialog("無檔案");
				}
			}
		});

		fileBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				selectVideoFromFile();
			}
		});

		videoBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dispatchTakeVideoIntent();
			}
		});

		listBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, VideoListActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.slide_in_left,
						android.R.anim.slide_out_right);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		userName = ((GlobalVariable) getApplication()).getLoginUserName();
		currentDateTimeString = DateFormat.getDateTimeInstance().format(
				new Date());
		tVideoDate.setText(currentDateTimeString);
	}

	@Override
	protected void onPause() {
		super.onPause();
		videoPath = null;
		tVideoPath.setText("");
		videoTitle.setText("");
		videoContext.setText("");
		videoPreview.setImageResource(R.drawable.video_icon);
	}

	@Override
	protected void onDestroy() {
		System.gc();
		super.onDestroy();
		tVideoDate.setText(null);
		tVideoPath.setText(null);
		videoPreview.setImageBitmap(null);
		videoTitle.setText(null);
		videoContext.setText(null);
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		if (backHandler != null) {
			backHandler.removeCallbacks(backRunnable);
		}
	}

	@Override
	public void onBackPressed() {
		if (doubleBackToExitPressedOnce) {
			super.onBackPressed();
			return;
		}
		doubleBackToExitPressedOnce = true;
		Toast.makeText(this, "在按一下返回鍵離開", Toast.LENGTH_SHORT).show();
		backHandler.postDelayed(backRunnable, 2000);
	}

	private void dispatchTakeVideoIntent() {
		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
		}
	}

	private void selectVideoFromFile() {
		Intent pickMedia = new Intent(Intent.ACTION_GET_CONTENT);
		pickMedia.setType("video/*");
		startActivityForResult(pickMedia, REQUEST_VIDEO_SELECT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == REQUEST_VIDEO_SELECT || requestCode == REQUEST_VIDEO_CAPTURE)
				&& resultCode == RESULT_OK) {
			Uri videoUri = data.getData();

			videoPath = getRealPathFromURI(this, videoUri);
			if (videoPath == null)
				videoPath = videoUri.getPath(); // from File Manager
			Log.d("", "Uri = " + videoPath);

			extensionName = videoPath.substring(videoPath.indexOf("."),
					videoPath.length());
			Log.d("", "extensionName = " + extensionName);
			
			tVideoPath.setText(videoPath);
			Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath,
					MediaStore.Images.Thumbnails.MICRO_KIND);
			videoPreview.setImageBitmap(thumbnail);
		}
	}

	private String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			if (cursor == null) {
				return null;
			}
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndexOrThrow(proj[0]);
			return cursor.getString(columnIndex);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.activity01, menu);
		return true;
	}

	/* 上傳檔至Server的方法 */
	private void uploadFile() {
		progressDialog = new ProgressDialog(MainActivity.this);
		progressDialog.setTitle("請稍候");
		progressDialog.setMessage("上傳中..");
		progressDialog.show();
		APIUploadThread uploadThread = new APIUploadThread(GlobalVariable.BASE
				+ APIConstant.Upload.UploadVideoFile);
		uploadThread.start();
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
			Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath,
					MediaStore.Images.Thumbnails.MICRO_KIND);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			byte[] imageBytes = baos.toByteArray();
			String encodedImage = Base64.encodeToString(imageBytes,
					Base64.DEFAULT);
			try {
				((JSONObject) json).put("image", encodedImage);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// Send the HttpPostRequest and receive a JSONObject in return
			String result = HttpUtil.connRemote(json, url);
			Message msg = new Message();
			msg.what = action;
			msg.obj = result;
			handler.sendMessage(msg);
		}
	};

	public class APIUploadThread extends Thread {

		private String actionUrl;

		public APIUploadThread(String url) {
			this.actionUrl = url;
		}

		@Override
		public void run() {
			String result = null;
			String end = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";
			HttpURLConnection con = null;
			BufferedInputStream bufferedInput = null;
			try {
				URL url = new URL(actionUrl);
				con = (HttpURLConnection) url.openConnection();
				/* 允許Input、Output，不使用Cache */
				con.setDoInput(true);
				con.setDoOutput(true);
				con.setUseCaches(false);
				con.setConnectTimeout(5000);
				/* 設置傳送的method=POST */
				con.setRequestMethod("POST");
				/* setRequestProperty */
				con.setRequestProperty("Connection", "Keep-Alive");
				con.setRequestProperty("Charset", "UTF-8");
				con.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);
				/* 分塊協議 */
				con.setChunkedStreamingMode(0);
				String filename = userName + extensionName;
				Log.i("", "filename= " + filename);
				/* 設置DataOutputStream */
				DataOutputStream ds = new DataOutputStream(
						con.getOutputStream());
				ds.writeBytes(twoHyphens + boundary + end);
				ds.writeBytes("Content-Disposition: form-data; "
						+ "name=\"file1\";filename=\"" + filename + "\"" + end);
				ds.writeBytes(end);
				/* 設置每次寫入8192bytes(8kb) */
				int bufferSize = 8192;
				/* 取得檔的FileInputStream */
				bufferedInput = new BufferedInputStream(new FileInputStream(
						videoPath), bufferSize);
				byte[] buffer = new byte[bufferSize];
				int copySize;
				/* 從檔讀取資料至緩衝區 */
				while ((copySize = bufferedInput.read(buffer)) > 0) {
					/* 將資料寫入DataOutputStream中 */
					ds.write(buffer, 0, copySize);
				}
				/* close streams */
				bufferedInput.close();
				ds.writeBytes(end);
				ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
				ds.flush();
				ds.close();
				/* 取得Response內容 */
				InputStream is = con.getInputStream();
				int ch;
				StringBuffer b = new StringBuffer();
				while ((ch = is.read()) != -1) {
					b.append((char) ch);
				}
				is.close();
				/* 將Response顯示 */
				result = b.toString().trim();
			} catch (Exception e) {
				e.printStackTrace();
			}
			con.disconnect();
			if (result == null || result.equals("")) {
				result = "error";
			}
			Message msg = new Message();
			msg.obj = result;
			msg.what = CODE_ACTION_UPLOAD_VIDEO;
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
				result = new JSONObject(URLDecoder.decode(msgResult.toString(),
						"utf-8"));
				String status = result.getString(HttpUtil.STATUS);
				String message = result.getString(HttpUtil.MESSAGE);
				if (status.equalsIgnoreCase(HttpUtil.SUCCESS)) {
					flag = true;
				} else {
					flag = false;
					showDialog(message); // 連線失敗
				}
				if (flag) {
					switch (msg.what) {
					case CODE_ACTION_UPLOAD_INFO:
						if (progressDialog != null) {
							progressDialog.dismiss();
						}
						if (status.equalsIgnoreCase(HttpUtil.SUCCESS)) {
							showDialog("上傳成功!");
						} else {
							showDialog("連接失敗");
						}
						break;
					case CODE_ACTION_UPLOAD_VIDEO:
						if (progressDialog != null) {
							progressDialog.dismiss();
						}
						if (status.equalsIgnoreCase(HttpUtil.SUCCESS)) {
							uploadVideoInfo(result.getString("video"));
						} else if (status.equalsIgnoreCase("failed")) {
							showDialog("上傳失敗");
						} else {
							showDialog("連接失敗");
						}
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

	private void uploadVideoInfo(String fileName) {
		progressDialog = new ProgressDialog(MainActivity.this);
		progressDialog.setTitle("請稍候");
		progressDialog.setMessage("上傳中..");
		progressDialog.show();
		JSONObject jsonObjSend = new JSONObject();
		try {
			String currentDateTime = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).format(new Date());
			// Add key/value pairs
			jsonObjSend.put("creat_time", currentDateTime);
			jsonObjSend.put("title", videoTitle.getText().toString());
			jsonObjSend.put("author", userName);
			jsonObjSend.put("describe", videoContext.getText().toString());
			jsonObjSend.put("video", fileName);
			jsonObjSend.put("permission", "private");
			// Output the JSON object we're sending to Logcat:
			Log.i("Activity", jsonObjSend.toString(2));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		APIJsonThread uploadThread = new APIJsonThread(GlobalVariable.BASE
				+ APIConstant.Upload.UploadVideoInfo, jsonObjSend,
				CODE_ACTION_UPLOAD_INFO);
		uploadThread.start();
	}

	/* 顯示Dialog的method */
	private void showDialog(final String mess) {
		try {
			new AlertDialog.Builder(MainActivity.this)
					.setTitle("Message")
					.setMessage(mess)
					.setNegativeButton("確定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									if (mess.equals("上傳成功!")) {
										Intent intent = new Intent();
										intent.setClass(MainActivity.this,
												VideoListActivity.class);
										startActivity(intent);
									}
								}
							}).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
