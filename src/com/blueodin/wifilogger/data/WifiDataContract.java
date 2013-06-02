package com.blueodin.wifilogger.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class WifiDataContract {
	public static final String AUTHORITY = "com.blueodin.wifilogger.provider";
	public static final String BASE_URI = "content://" + AUTHORITY + "/";

	public static class Network {
		public static final String PATH = "networks";
		public static final String PATH_BY_ID = "network/id/";
		public static final String PATH_BY_BSSID = "network/bssid/";
		
		public static final Uri CONTENT_URI = Uri.parse(BASE_URI + PATH);
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(BASE_URI + PATH_BY_ID);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(BASE_URI + PATH_BY_ID + "#");

        public static final Uri CONTENT_BSSID_URI_BASE = Uri.parse(BASE_URI + PATH_BY_BSSID);
        public static final Uri CONTENT_BSSID_URI_PATTERN = Uri.parse(BASE_URI + PATH_BY_BSSID + "*");
        
        public static Uri uriById(long id) {
        	return Uri.parse(BASE_URI + PATH_BY_ID + id);
        }
        
        public static Uri uriByBSSID(String bssid) {
        	return Uri.parse(BASE_URI + PATH_BY_BSSID + bssid);
        }
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + ".networks";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + ".network";
        
        public static final String DEFAULT_ORDER_BY = Columns._ID + " DESC";

		public static class Columns implements BaseColumns {
			public static final String BSSID = "bssid";
			public static final String SSID = "ssid";
			public static final String FREQUENCY = "frequency";
			public static final String CAPABILITIES = "capabilities";
			public static final String LASTTIME = "lasttime";
		}
	}
	
	public static class Location {
		public static final String PATH = "locations";
		public static final String PATH_BY_ID = "location/id/";
		public static final String PATH_BY_BSSID = "location/bssid/";
		
		public static final Uri CONTENT_URI = Uri.parse(BASE_URI + PATH);
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(BASE_URI + PATH_BY_ID);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(BASE_URI + PATH_BY_ID + "#");

        public static final Uri CONTENT_BSSID_URI_BASE = Uri.parse(BASE_URI + PATH_BY_BSSID);
        public static final Uri CONTENT_BSSID_URI_PATTERN = Uri.parse(BASE_URI + PATH_BY_BSSID + "*");
        
        public static Uri uriById(long id) {
        	return Uri.parse(BASE_URI + PATH_BY_ID + id);
        }
        
        public static Uri uriByBSSID(String bssid) {
        	return Uri.parse(BASE_URI + PATH_BY_BSSID + bssid);
        }
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + ".locations";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + ".location";
        
        public static final String DEFAULT_ORDER_BY = Columns.TIMESTAMP + " DESC";

		public static class Columns implements BaseColumns {
			public static final String BSSID = "bssid";
			public static final String SSID = "ssid";
			public static final String LEVEL = "level";
			public static final String TIMESTAMP = "timestamp";
		}
	}
}
