package com.blueodin.wifilogger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class WifiScanManager extends BroadcastReceiver {
	private PendingIntent mPendingIntent;
	private AlarmManager mAlarmManager;
	private Context mContext;
	private int mScanInterval;
	
	private static WifiLock mWifiLock;
	private static WakeLock mWakeLock;
	
	private static final String TAG = "WifiScanManager";
	
	public WifiScanManager() { }
	
	public WifiScanManager(Context context, int scanInterval) {
		mContext = context;
		mScanInterval = scanInterval;
		
		mWifiLock = ((WifiManager)context.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, TAG + "_WifiLock");
		mWakeLock = ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG + "_WakeLock");
		mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	}
	
	public void start() {
		Intent intent = new Intent(mContext, WifiScanManager.class);
		mPendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), mScanInterval, mPendingIntent);
	}
	
	public void stop() {
		mAlarmManager.cancel(mPendingIntent);
	}
	
	public static void lock() {
		try {
			mWakeLock.acquire();
			mWifiLock.acquire();
		} catch(Exception ex) {
			ex.printStackTrace();
			Log.e(TAG, "Error getting lock: " + ex.getMessage());
		}
	}
	
	public static void unlock() {
		if (mWakeLock.isHeld())
			mWakeLock.release();
		
		if (mWifiLock.isHeld())
			mWifiLock.release();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		
		if(wifiManager.isWifiEnabled()) {
			lock();
			wifiManager.startScan();
		}
	}
}
