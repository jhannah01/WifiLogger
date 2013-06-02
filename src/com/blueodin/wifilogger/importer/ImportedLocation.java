package com.blueodin.wifilogger.importer;

import android.database.Cursor;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

public abstract class ImportedLocation {
	public String bssid;
	public int level;
	public float latitude;
	public float longitude;
	public int altitude;
	public int accuracy;
	public long timestamp;
	
	public ImportedLocation(String bssid, int level, float latitude, float longitude, int altitude, int accuracy, long timestamp) {
		this.bssid = bssid;
		this.level = level;
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.accuracy = accuracy;
		this.timestamp = timestamp;
	}
	
	public ImportedLocation(Cursor locationData) {
		parseLocation(locationData);
	}
	
	protected ImportedLocation() {
		
	}
	
	public String getFormattedTimestamp() {
		return DateFormat.format("MMM dd, yyyy h:mmaa", this.timestamp).toString();
	}
	
	public String getRelativeTimestamp() {
		return DateUtils.getRelativeTimeSpanString(this.timestamp).toString();
	}
	
	public abstract void parseLocation(Cursor locationData);
}