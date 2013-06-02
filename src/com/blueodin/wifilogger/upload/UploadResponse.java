package com.blueodin.wifilogger.upload;

import android.text.TextUtils;

public class UploadResponse {
	public boolean success;
	public String message;
	
	public UploadResponse(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	
	@Override
	public String toString() {
		String result = (success ? "Success" : "Error");
		if(TextUtils.isEmpty(message)) {
			if(!success)
				return String.format("%s: Unknown Error", result);
			
			return result;
		}
		
		return String.format("%s: %s", result, message);
	}
}