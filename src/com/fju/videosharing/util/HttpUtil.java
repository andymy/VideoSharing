package com.fju.videosharing.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class HttpUtil {
	private static final String TAG = "HttpUtil";

	public static final String PARAM = "params";
	public static final String STATUS = "status";
	public static final String MESSAGE = "message";
	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
	public static final String WARN = "warn";
	public static final int CONNECTION_TIMEOUT = 1000 * 60;
	public static final int SO_TIMEOUT = 1000 * 60;

	public static String connRemote(Object jsonObject, String url) {
		String retSrc = "";
		try {
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpPost httpPost = new HttpPost(url);
			httpPost.setParams(httpParams);
			httpPost.setEntity(new ByteArrayEntity(URLEncoder.encode(jsonObject.toString(), "UTF-8").getBytes("UTF-8")));
			HttpResponse response = httpClient.execute(httpPost);

			// List<NameValuePair> nameValuePair = new
			// ArrayList<NameValuePair>();
			// nameValuePair.add(new BasicNameValuePair(PARAM,
			// jsonObject.toString()));
			// httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair,
			// HTTP.UTF_8));
			// httpPost.setParams(httpParams);
			// HttpResponse response = httpClient.execute(httpPost);
			retSrc = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			try {
				JSONObject errorJson = new JSONObject();
				errorJson.put(STATUS, ERROR);
				errorJson.put(MESSAGE, "連線失敗");
				retSrc = errorJson.toString();
			} catch (JSONException e1) {
				Log.e(TAG, e.getMessage());
			}
			Log.e(TAG, e.toString());
		}
		return retSrc;
	}

	public static String httpPostQuery(String hostURL, String name, String pwd) {
		String result = "";
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		HttpPost httpQuery = new HttpPost(hostURL);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("username", name));
		pairs.add(new BasicNameValuePair("password", pwd));
		try {
			// Assign the list as the arguments of post being UTF_8 encoding.
			httpQuery.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
			httpResponse = httpClient.execute(httpQuery);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {// 判斷回傳的狀態是不是200
				result = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (ClientProtocolException ex) {
			ex.printStackTrace();
			Log.e("", "ClientProtocolException:" + ex.getMessage());
			result = "error";
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.e("", "IOException:" + ex.getMessage());
			result = "error";
		} finally {
			// The HTTP connection must be closed any way.
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	public static String httpPostCheck(String hostURL, String name) {
		String result = "";
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		HttpPost httpQuery = new HttpPost(hostURL);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("username", name));
		try {
			// Assign the list as the arguments of post being UTF_8 encoding.
			httpQuery.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
			httpResponse = httpClient.execute(httpQuery);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {// 判斷回傳的狀態是不是200
				result = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (ClientProtocolException ex) {
			ex.printStackTrace();
			Log.e("", "ClientProtocolException:" + ex.getMessage());
			result = "error";
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.e("", "IOException:" + ex.getMessage());
			result = "error";
		} finally {
			// The HTTP connection must be closed any way.
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}
}
