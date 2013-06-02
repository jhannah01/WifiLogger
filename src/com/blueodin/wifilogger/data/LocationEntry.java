package com.blueodin.wifilogger.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import com.blueodin.wifilogger.importer.ImportedLocation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocationEntry {
	public final String bssid;
	public final String ssid;
	public final int level;
	public final long timestamp;
	public boolean synced = false;
	
	public LocationEntry(String ssid, ImportedLocation location) {
		this.bssid = location.bssid;
		this.ssid = ssid;
		this.level = location.level;
		this.timestamp = location.timestamp;
	}
	
	public LocationEntry(String bssid, String ssid, int level, long timestamp) {
		this.bssid = bssid;
		this.ssid = ssid;
		this.level = level;
		this.timestamp = timestamp;
	}
	
	protected LocationEntry(Cursor data) {
		this.bssid = data.getString(data.getColumnIndex(WifiDataContract.Location.Columns.BSSID));
		this.ssid = data.getString(data.getColumnIndex(WifiDataContract.Location.Columns.SSID));
		this.level = data.getInt(data.getColumnIndex(WifiDataContract.Location.Columns.LEVEL));
		this.timestamp = data.getLong(data.getColumnIndex(WifiDataContract.Location.Columns.TIMESTAMP));
	}
	
	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		
		values.put(WifiDataContract.Location.Columns.BSSID, this.bssid);
		values.put(WifiDataContract.Location.Columns.SSID, this.ssid);
		values.put(WifiDataContract.Location.Columns.LEVEL, this.level);
		values.put(WifiDataContract.Location.Columns.TIMESTAMP, this.timestamp);
		
		return values;
	}
	
	public static void sortList(List<LocationEntry> locations) {
		Collections.sort(locations, new Comparator<LocationEntry>() {
			@Override
			public int compare(LocationEntry lhs, LocationEntry rhs) {
				if(lhs.timestamp < rhs.timestamp)
					return -1;
				
				if(lhs.timestamp > rhs.timestamp)
					return 1;
				
				return 0;
			}
		});
	}
	
	public String getFormattedTimestamp() {
		return DateFormat.format("MMM dd, yyyy h:mmaa", this.timestamp).toString();
	}
	
	public String getRelativeTimestamp() {
		return DateUtils.getRelativeTimeSpanString(this.timestamp).toString();
	}
	
	@Override
	public String toString() {
		return String.format("%s [%s] - %d dBm @ %s", this.ssid, this.bssid, this.level, this.getFormattedTimestamp());
	}

	public void setSynced(ContentResolver resolver) {
		synced = true;
		ContentValues values = new ContentValues();
		values.put("synced", true);
		resolver.update(WifiDataContract.Location.uriByBSSID(bssid), values, null, null);
	}
}