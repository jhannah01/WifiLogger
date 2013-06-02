package com.blueodin.wifilogger.importer.wigle;

import android.database.Cursor;
import android.provider.BaseColumns;

import com.blueodin.wifilogger.importer.ImportedNetwork;

public class WigleNetwork extends ImportedNetwork {
	public WigleNetwork(Cursor networkData) {
		super(networkData);
	}
	
	@Override
	public void parseNetwork(Cursor networkData) {
		this.bssid = networkData.getString(networkData.getColumnIndex(Columns.BSSID));
		this.ssid = networkData.getString(networkData.getColumnIndex(Columns.SSID));
		this.frequency = networkData.getInt(networkData.getColumnIndex(Columns.FREQUENCY));
		this.capabilities = networkData.getString(networkData.getColumnIndex(Columns.CAPABILITIES));
		this.lasttime = networkData.getLong(networkData.getColumnIndex(Columns.LASTTIME));
		this.lastlat = networkData.getFloat(networkData.getColumnIndex(Columns.LASTLAT));
		this.lastlon = networkData.getFloat(networkData.getColumnIndex(Columns.LASTLON));
	}
	
	public static class Columns implements BaseColumns {
		public static final String BSSID = "bssid";
		public static final String SSID = "ssid";
		public static final String FREQUENCY = "frequency";
		public static final String CAPABILITIES = "capabilities";
		public static final String LASTTIME = "lasttime";
		public static final String LASTLAT = "lastlat";
		public static final String LASTLON = "lastlon";
	}
}