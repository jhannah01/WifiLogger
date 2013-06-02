package com.blueodin.wifilogger.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;


import java.util.HashMap;

public class WifiDataProvider extends ContentProvider {
	private DBHelper mDbHelper;

	private static final UriMatcher sUriMatcher;
	private static final int URIMATCH_NETWORKS = 0x02;
	private static final int URIMATCH_NETWORK_BY_ID = 0x03;
	private static final int URIMATCH_NETWORK_BY_BSSID = 0x04;
	private static final int URIMATCH_LOCATIONS = 0x05;
	private static final int URIMATCH_LOCATION_BY_ID = 0x06;
	private static final int URIMATCH_LOCATION_BY_BSSID = 0x07;
	
	public static final HashMap<String, String> networksProjection = new HashMap<String, String>();
	public static final HashMap<String, String> locationsProjection = new HashMap<String, String>();
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(WifiDataContract.AUTHORITY, WifiDataContract.Network.PATH, URIMATCH_NETWORKS);
		sUriMatcher.addURI(WifiDataContract.AUTHORITY, WifiDataContract.Network.PATH_BY_ID + "#", URIMATCH_NETWORK_BY_ID);
		sUriMatcher.addURI(WifiDataContract.AUTHORITY, WifiDataContract.Network.PATH_BY_BSSID + "*", URIMATCH_NETWORK_BY_BSSID);
		sUriMatcher.addURI(WifiDataContract.AUTHORITY, WifiDataContract.Location.PATH, URIMATCH_LOCATIONS);
		sUriMatcher.addURI(WifiDataContract.AUTHORITY, WifiDataContract.Location.PATH_BY_ID + "#", URIMATCH_LOCATION_BY_ID);
		sUriMatcher.addURI(WifiDataContract.AUTHORITY, WifiDataContract.Location.PATH_BY_BSSID + "*", URIMATCH_LOCATION_BY_BSSID);
	
		networksProjection.put(WifiDataContract.Network.Columns._ID, WifiDataContract.Network.Columns._ID);
		networksProjection.put(WifiDataContract.Network.Columns.BSSID, WifiDataContract.Network.Columns.BSSID);
		networksProjection.put(WifiDataContract.Network.Columns.SSID, WifiDataContract.Network.Columns.SSID);
		networksProjection.put(WifiDataContract.Network.Columns.FREQUENCY, WifiDataContract.Network.Columns.FREQUENCY);
		networksProjection.put(WifiDataContract.Network.Columns.CAPABILITIES, WifiDataContract.Network.Columns.CAPABILITIES);
		networksProjection.put(WifiDataContract.Network.Columns.LASTTIME, WifiDataContract.Network.Columns.LASTTIME);
		
		locationsProjection.put(WifiDataContract.Location.Columns._ID, WifiDataContract.Location.Columns._ID);
		locationsProjection.put(WifiDataContract.Location.Columns.BSSID, WifiDataContract.Location.Columns.BSSID);
		locationsProjection.put(WifiDataContract.Location.Columns.SSID, WifiDataContract.Location.Columns.SSID);
		locationsProjection.put(WifiDataContract.Location.Columns.LEVEL, WifiDataContract.Location.Columns.LEVEL);
		locationsProjection.put(WifiDataContract.Location.Columns.TIMESTAMP, WifiDataContract.Location.Columns.TIMESTAMP);
	}
	
	public WifiDataProvider() { }
	
	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case URIMATCH_NETWORKS:
			return WifiDataContract.Network.CONTENT_TYPE;
		case URIMATCH_NETWORK_BY_ID:
		case URIMATCH_NETWORK_BY_BSSID:
			return WifiDataContract.Network.CONTENT_ITEM_TYPE;
		case URIMATCH_LOCATIONS:
			return WifiDataContract.Location.CONTENT_TYPE;
		case URIMATCH_LOCATION_BY_ID:
		case URIMATCH_LOCATION_BY_BSSID:
			return WifiDataContract.Location.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		mDbHelper = new DBHelper(getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String orderBy = null;
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		switch(sUriMatcher.match(uri)) {
		case URIMATCH_NETWORKS:
		case URIMATCH_NETWORK_BY_ID:
		case URIMATCH_NETWORK_BY_BSSID:
			queryBuilder.setTables(DBHelper.TABLE_NETWORKS);
			queryBuilder.setProjectionMap(networksProjection);
			orderBy = WifiDataContract.Network.DEFAULT_ORDER_BY;
			break;
		case URIMATCH_LOCATIONS:
		case URIMATCH_LOCATION_BY_ID:
		case URIMATCH_LOCATION_BY_BSSID:
			queryBuilder.setTables(DBHelper.TABLE_LOCATIONS);
			queryBuilder.setProjectionMap(locationsProjection);
			orderBy = WifiDataContract.Location.DEFAULT_ORDER_BY;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		switch(sUriMatcher.match(uri)) {
		case URIMATCH_NETWORK_BY_ID:
			queryBuilder.appendWhere(WifiDataContract.Network.Columns._ID + " = " + uri.getPathSegments().get(1));
			break;
		case URIMATCH_NETWORK_BY_BSSID:
			queryBuilder.appendWhere(WifiDataContract.Network.Columns.BSSID + " = '" + uri.getPathSegments().get(1) + "'");
			break;
		case URIMATCH_LOCATION_BY_ID:
			queryBuilder.appendWhere(WifiDataContract.Location.Columns._ID + " = " + uri.getPathSegments().get(1));
			break;
		case URIMATCH_LOCATION_BY_BSSID:
			queryBuilder.appendWhere(WifiDataContract.Location.Columns.BSSID + " = '" + uri.getPathSegments().get(1) + "'");
			break;
		}
		
		if(!TextUtils.isEmpty(sortOrder))
			orderBy = sortOrder;
		
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor c = queryBuilder.query(db, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		return c;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri baseUri;
		String tableName;
		
		switch(sUriMatcher.match(uri)) {
		case URIMATCH_NETWORKS:
			baseUri = WifiDataContract.Network.CONTENT_ID_URI_BASE;
			tableName = DBHelper.TABLE_NETWORKS;
			break;
		case URIMATCH_LOCATIONS:
			baseUri = WifiDataContract.Location.CONTENT_ID_URI_BASE;
			tableName = DBHelper.TABLE_LOCATIONS;
			break;
		default:
			throw new IllegalArgumentException("Invalid URI: " + uri);
		}
		
		if (values == null)
			values = new ContentValues();
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		long rowId = db.insert(tableName, null, values);
		
		if(rowId < 1)
			throw new SQLException("Failed to insert row for URI: " + uri);
		
		Uri insertedUri = ContentUris.withAppendedId(baseUri, rowId);
		
		getContext().getContentResolver().notifyChange(insertedUri, null);

		db.close();
		
		return insertedUri;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String tableName;
		
		switch(sUriMatcher.match(uri)) {
		case URIMATCH_NETWORKS:
			tableName = DBHelper.TABLE_NETWORKS;
			break;
		case URIMATCH_LOCATIONS:
			tableName = DBHelper.TABLE_LOCATIONS;
			break;
		default:
			throw new IllegalArgumentException("Invalid URI: " + uri);
		}
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		int c = db.delete(tableName, selection, selectionArgs);
		
		db.close();
		
		return c;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		String tableName;
		
		switch(sUriMatcher.match(uri)) {
		case URIMATCH_NETWORKS:
			tableName = DBHelper.TABLE_NETWORKS;
			break;
		case URIMATCH_LOCATIONS:
			tableName = DBHelper.TABLE_LOCATIONS;
			break;
		default: 
			throw new IllegalArgumentException("Invalid URI: " + uri);
		}
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		int c = db.update(tableName, values, selection, selectionArgs);
		
		db.close();
		
		return c;
	}
}
