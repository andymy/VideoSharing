package com.fju.videosharing.util;

public interface APIConstant {
	public static final String Login = "/webtest/login.do";
	public static final String SearchVideo = "/webtest/searchVideo.do";

	public interface Register {
		public static final String Register = "/webtest/register.do";
		public static final String CheckUserExist = "/webtest/check.do";
	}

	public interface Upload {
		public static final String UploadVideoFile = "/webtest/upload.do";
		public static final String UploadVideoInfo = "/webtest/uploadInfo.do";
	}
}
