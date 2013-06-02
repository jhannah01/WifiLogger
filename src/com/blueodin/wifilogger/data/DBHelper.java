package com.blueodin.wifilogger.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	public static final String TABLE_NETWORKS = "networks";
	public static final String TABLE_LOCATIONS = "locations";
	
	private static final String DATABASE_NAME = "wifidata.sqlite";
	private static final int DATABASE_VERSION = 9;
	
	private static final String SQL_CREATE_NETWORKS_TABLE = "CREATE TABLE " + TABLE_NETWORKS + " (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"bssid TEXT UNIQUE NOT NULL, " +
			"ssid TEXT NOT NULL, " +
			"frequency INTEGER, " +
			"capabilities TEXT, " +
			"lasttime INTEGER);";
	
	private static final String SQL_CREATE_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + " (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"bssid TEXT NOT NULL, " +
			"ssid TEXT NOT NULL, " +
			"level INTEGER, " +
			"synced BOOLEAN, " +
			"timestamp INTEGER);";
	
	private static final String SQL_CREATE_TRIGGER_UPDATE_LASTTIME = "CREATE TRIGGER update_network_lasttime" +
			" AFTER INSERT ON " + TABLE_LOCATIONS + 
			" BEGIN " +
				"UPDATE " + TABLE_NETWORKS + " SET lasttime = new.timestamp WHERE " + TABLE_NETWORKS + ".bssid = new.bssid; " + 
			"END;";
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_NETWORKS_TABLE);
		db.execSQL(SQL_CREATE_LOCATIONS_TABLE);
		db.execSQL(SQL_CREATE_TRIGGER_UPDATE_LASTTIME);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NETWORKS + ";");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS + ";");
		db.execSQL("DROP TRIGGER IF EXISTS update_network_lasttime;");
		onCreate(db);
	}
}
