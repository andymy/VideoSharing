package com.fju.videosharing.util;

import android.app.Application;

public class GlobalVariable extends Application {

	private String loginUserName;
	public static String BASE = "http://192.168.1.6:8080";

	public void setIpAddress(String ip) {
		GlobalVariable.BASE = "http://" + ip;
	}

	public String getLoginUserName() {
		return loginUserName;
	}

	public void setLoginUserName(String loginUserName) {
		this.loginUserName = loginUserName;
	}

}
