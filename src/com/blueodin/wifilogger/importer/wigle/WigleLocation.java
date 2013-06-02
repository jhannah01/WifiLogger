package com.blueodin.wifilogger.importer.wigle;

import android.database.Cursor;
import android.provider.BaseColumns;

import com.blueodin.wifilogger.importer.ImportedLocation;

public class WigleLocation extends ImportedLocation {
	public WigleLocation(Cursor locationData) {
		super(locationData);
	}
	
	@Override
	public void parseLocation(Cursor locationData) {
		this.bssid = locationData.getString(locationData.getColumnIndex(Columns.BSSID));
		this.level = locationData.getInt(locationData.getColumnIndex(Columns.LEVEL));
		this.latitude = locationData.getFloat(locationData.getColumnIndex(Columns.LATITUDE));
		this.longitude = locationData.getFloat(locationData.getColumnIndex(Columns.LONGTIUDE));
		this.altitude = locationData.getInt(locationData.getColumnIndex(Columns.ALTITUDE));
		this.accuracy = locationData.getInt(locationData.getColumnIndex(Columns.ACCURACY));
		this.timestamp = locationData.getLong(locationData.getColumnIndex(Columns.TIME));
	}
	
	public static class Columns implements BaseColumns {
		public static final String BSSID = "bssid";
		public static final String LEVEL = "level";
		public static final String LATITUDE = "lat";
		public static final String LONGTIUDE = "lon";
		public static final String ALTITUDE = "altitude";
		public static final String ACCURACY = "accuracy";
		public static final String TIME = "time";
	}
}