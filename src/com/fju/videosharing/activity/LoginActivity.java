package com.fju.videosharing.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.fju.videosharing.R;
import com.fju.videosharing.util.APIConstant;
import com.fju.videosharing.util.GlobalVariable;
import com.fju.videosharing.util.HttpUtil;
import com.fju.videosharing.view.GIFView;

public class LoginActivity extends Activity {

	private Button cancelBtn, loginBtn;
	private Button regBtn, tryBtn;
	private EditText userEditText, pwdEditText;
	private ProgressBar loginProgress;
	private ImageView setting;
	private GIFView imageViewGIF;
	private boolean isLogin = false; // 正在請求中
	private static final int CODE_ACTION_LOGIN = 100;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_layout);

		cancelBtn = (Button) findViewById(R.id.btn_cancel);
		loginBtn = (Button) findViewById(R.id.btn_login);
		regBtn = (Button) findViewById(R.id.btn_register);
		tryBtn = (Button) findViewById(R.id.btn_try);

		setting = (ImageView) findViewById(R.id.setting_btn);

		loginProgress = (ProgressBar) findViewById(R.id.progress_login);

		userEditText = (EditText) findViewById(R.id.edit_user);
		pwdEditText = (EditText) findViewById(R.id.edit_pwd);

		cancelBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		loginBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isLogin) {
					if (validate()) {
						hideSoftKeyboard(LoginActivity.this);
						login();
					}
				}
			}
		});
		regBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this,
						RegisterActivity.class);
				startActivity(intent);
			}
		});
		tryBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String android_id = Secure.getString(LoginActivity.this
						.getBaseContext().getContentResolver(),
						Secure.ANDROID_ID);
				Log.i("guest = ", android_id);
				((GlobalVariable) LoginActivity.this.getApplication())
						.setLoginUserName(android_id);
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				startActivity(intent);
				LoginActivity.this.finish();
			}
		});
		setting.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				imageViewGIF.setVisibility(View.VISIBLE);
				AlertDialog.Builder builder = new AlertDialog.Builder(
						LoginActivity.this);
				builder.setTitle("IP設定");
				builder.setMessage("Change IP:").setCancelable(false);
				final EditText e = new EditText(LoginActivity.this);
				e.setText(GlobalVariable.BASE.substring(GlobalVariable.BASE
						.indexOf("//") + 2));
				builder.setView(e);
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								((GlobalVariable) getApplication())
										.setIpAddress(e.getText().toString()
												.trim());
								imageViewGIF.setVisibility(View.INVISIBLE);
								return;
							}
						});
				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								imageViewGIF.setVisibility(View.INVISIBLE);
								return;
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		pwdEditText.setText("");
	}

	@Override
	protected void onPause() {
		loginProgress.setVisibility(View.INVISIBLE);
		isLogin = false;
		super.onPause();
	}

	public static void hideSoftKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager) activity
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (activity.getCurrentFocus() != null) {
			inputMethodManager.hideSoftInputFromWindow(activity
					.getCurrentFocus().getWindowToken(), 0);
		}
	}

	private void login() {
		isLogin = true; // 開始請求
		loginProgress.setVisibility(View.VISIBLE);
		String username = userEditText.getText().toString();
		String pwd = pwdEditText.getText().toString();
		JSONObject jsonObjSend = new JSONObject();
		try {
			jsonObjSend.put("loginName", username);
			jsonObjSend.put("loginPwd", pwd);
			Log.i("Activity", jsonObjSend.toString(2));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		APIJsonThread loginThread = new APIJsonThread(GlobalVariable.BASE
				+ APIConstant.Login, jsonObjSend, CODE_ACTION_LOGIN);
		loginThread.start();
	}

	private boolean validate() {
		String username = userEditText.getText().toString();
		if (username.equals("") || username == null) {
			showDialog("請輸入帳號");
			return false;
		}
		String pwd = pwdEditText.getText().toString();
		if (pwd.equals("") || username == null) {
			showDialog("請輸入密碼");
			return false;
		}
		return true;
	}

	private void showDialog(String msg) {
		loginProgress.setVisibility(View.INVISIBLE);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg).setCancelable(false)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
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
				isLogin = false;
				if (flag) {
					switch (msg.what) {
					case CODE_ACTION_LOGIN:
						if (result.getBoolean("loginFlg")) {
							String userName = result.getString("loginUser");
							((GlobalVariable) LoginActivity.this
									.getApplication())
									.setLoginUserName(userName);
							Intent intent = new Intent(LoginActivity.this,
									MainActivity.class);
							startActivity(intent);
							overridePendingTransition(
									android.R.anim.slide_in_left,
									android.R.anim.slide_out_right);
							LoginActivity.this.finish();
						} else {
							showDialog(message);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	};
}