package com.blueodin.wifilogger.importer;

import android.database.Cursor;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

public abstract class ImportedNetwork {
	public String bssid;
	public String ssid;
	public int frequency;
	public String capabilities;
	public long lasttime;
	public float lastlat;
	public float lastlon;
	
	protected ImportedNetwork() {
		
	}
	
	public ImportedNetwork(String bssid, String ssid, int frequency, String capabilities, long lasttime, float lastlat, float lastlon) {
		this.bssid = bssid;
		this.ssid = ssid;
		this.frequency = frequency;
		this.capabilities = capabilities;
		this.lasttime = lasttime;
		this.lastlat = lastlat;
		this.lastlon = lastlon;
	}
	
	public ImportedNetwork(Cursor networkData) {
		parseNetwork(networkData);
	}
	
	public String getFormattedLastTime() {
		return DateFormat.format("MMM dd, yyyy h:mmaa", this.lasttime).toString();
	}
	
	public String getRelativeLastTime() {
		return DateUtils.getRelativeTimeSpanString(this.lasttime).toString();
	}
	
	public abstract void parseNetwork(Cursor networkData);
}