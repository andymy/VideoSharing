package com.fju.videosharing.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.fju.videosharing.R;
import com.fju.videosharing.util.APIConstant;
import com.fju.videosharing.util.GlobalVariable;
import com.fju.videosharing.util.HttpUtil;

public class RegisterActivity extends Activity {

	private static final int CODE_ACTION_CHECK = 100;
	private static final int CODE_ACTION_REG = 101;
	private EditText userNameEdT;
	private EditText userPwdEdT;
	private EditText userPwdConfirmEdT;
	private Button done;
	private ImageView iconCheck;
	private ProgressDialog progressDialog;
	private boolean isUserExist = true;
	private boolean checkReponse = true;
	private String sentName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reg_layout);
		userNameEdT = (EditText) findViewById(R.id.editText_account);
		userPwdEdT = (EditText) findViewById(R.id.editText_pwd);
		userPwdConfirmEdT = (EditText) findViewById(R.id.editText_pwd_confirm);
		iconCheck = (ImageView) findViewById(R.id.image_check);

		userNameEdT.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				sendCheck();
			}
		});
		InputFilter filter = new InputFilter() {

			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (!Character.isLetterOrDigit(source.charAt(i))) {
						return "";
					}
				}
				return null;
			}
		};
		userNameEdT.setFilters(new InputFilter[] { filter });

		done = (Button) findViewById(R.id.btn_done);
		done.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (confirm()) {
					progressDialog = new ProgressDialog(RegisterActivity.this);
					progressDialog.setTitle("請稍候");
					progressDialog.setMessage("註冊中..");
					progressDialog.show();
					String username = userNameEdT.getText().toString();
					String userPwd = userPwdEdT.getText().toString();
					APIRegThread regThread = new APIRegThread(GlobalVariable.BASE + APIConstant.Register.Register, username, userPwd);
					regThread.start();
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		System.gc();
		super.onDestroy();
		iconCheck.setImageResource(0);
	}

	public void sendCheck() {
		if (checkReponse) {
			sentName = userNameEdT.getText().toString();
			if (!sentName.equals("")) {
				Log.i("", "send " + sentName);
				iconCheck.setImageResource(0);
				checkReponse = false;
				APICheckThread checkThread = new APICheckThread(GlobalVariable.BASE + APIConstant.Register.CheckUserExist, sentName);
				checkThread.start();
			} else {
				isUserExist = true;
				iconCheck.setImageResource(R.drawable.icon_danger);
			}
		} else
			Log.i("", "no send " + userNameEdT.getText().toString());
	}

	public boolean confirm() {
		String username = userNameEdT.getText().toString();
		if (username.equals("") || username == null) {
			showDialog("請輸入帳號");
			return false;
		}
		if (isUserExist) {
			showDialog("帳號已存在");
			return false;
		}
		String userPwd = userPwdEdT.getText().toString();
		if (userPwd == null || userPwd.equals("")) {
			showDialog("請輸入密碼");
			return false;
		}
		String userPwdConfirm = userPwdConfirmEdT.getText().toString();
		if (userPwdConfirm == null || userPwdConfirm.equals("")) {
			showDialog("請輸入確認密碼");
			return false;
		}
		if (!userPwd.equals(userPwdConfirm)) {
			showDialog("確認密碼不符");
			return false;
		}
		return true;
	}

	private void showDialog(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg).setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public class APICheckThread extends Thread {

		private String url;
		private String userName;

		public APICheckThread(String url, String userName) {
			this.url = url;
			this.userName = userName;
		}

		@Override
		public void run() {
			String result = HttpUtil.httpPostCheck(url, userName);
			Message msg = new Message();
			msg.obj = result;
			msg.what = CODE_ACTION_CHECK;
			handler.sendMessage(msg);
		}
	};

	public class APIRegThread extends Thread {

		private String url;
		private String userName;
		private String userPwd;

		public APIRegThread(String url, String userName, String userPwd) {
			this.url = url;
			this.userName = userName;
			this.userPwd = userPwd;
		}

		@Override
		public void run() {
			String result = HttpUtil.httpPostQuery(url, userName, userPwd);
			Message msg = new Message();
			msg.obj = result;
			msg.what = CODE_ACTION_REG;
			handler.sendMessage(msg);
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String msgResult = (String) msg.obj;
			switch (msg.what) {
			case CODE_ACTION_CHECK:
				checkReponse = true;
				if (!sentName.equals(userNameEdT.getText().toString())) {
					sendCheck();
					break;
				}
				if (msgResult.equalsIgnoreCase("true")) {
					isUserExist = true;
					iconCheck.setImageResource(R.drawable.icon_danger);
				} else if (msgResult.equalsIgnoreCase("false")) {
					isUserExist = false;
					iconCheck.setImageResource(R.drawable.icon_ok);
				} else {
					// showDialog("連接失敗");
				}
				break;
			case CODE_ACTION_REG:
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				if (msgResult.equalsIgnoreCase(HttpUtil.SUCCESS)) {
					((GlobalVariable) RegisterActivity.this.getApplication()).setLoginUserName(userNameEdT.getText().toString());
					Intent intent = new Intent();
					intent.setClass(RegisterActivity.this, MainActivity.class);
					startActivity(intent);
					RegisterActivity.this.finish();
				} else if (msgResult.equalsIgnoreCase(HttpUtil.WARN)) {
					showDialog("此帳號已被註冊");
				} else {
					showDialog("連接失敗");
				}
				break;
			}
		}
	};
}
