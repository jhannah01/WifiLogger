package com.blueodin.wifilogger;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

public class WifiLockService extends Service {
	private static final String PARAM_SCAN_INTERVAL = "param_scan_interval";
	private static final int NOTIFICATION_ID = 2;
	
	private static final String TAG = "WifiLockService";
	
	private final IBinder mBinder = new WifiLockServiceBinder();
	private boolean mIsLogging = false;
	
	private NotificationManager mNotificationManager;
	private int mScanInterval = 3 * 1000;
	private WifiScanManager mWifiScanManager;
	
	public WifiLockService() { }

	public class WifiLockServiceBinder extends Binder {
		public WifiLockService getService() {
			return WifiLockService.this;
		}
	}
	
	private void showNotification(String textContent) {
		mNotificationManager.notify(NOTIFICATION_ID, getNotificationBuilder(textContent).build());
	}
	
	private NotificationCompat.Builder getNotificationBuilder(String textContent) {
		Intent mainIntent = new Intent(this, MainActivity.class);
		mainIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		mainIntent.putExtra(MainActivity.PARAM_FROM_SERVICE, true);
		
		return (new NotificationCompat.Builder(this))
			.setContentTitle("WifiLogger")
			.setContentText(textContent)
			.setSmallIcon(R.drawable.ic_stat_service)
			.setContentIntent(PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT))
			.setWhen(System.currentTimeMillis())
			.setAutoCancel(false)
			.setOngoing(true);
	}
	
	public void updateNotification(String subText) {
		updateNotification("", subText);
	}
	
	public void updateNotification(String tickerText, String subText) {
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
		
		notificationBuilder
			.setSubText(subText)
			.setWhen(System.currentTimeMillis());
		
		if(!TextUtils.isEmpty(tickerText))
			notificationBuilder.setTicker(tickerText);
		
		mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}
	
	private void cancelNotification() {
		mNotificationManager.cancel(NOTIFICATION_ID);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "Service binding...");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "Service unbinding...");
		
		return true;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		this.mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		this.mWifiScanManager = new WifiScanManager(this, mScanInterval);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(mIsLogging)
			stopLogging();
		
		Log.d(TAG, "Service destroyed...");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null) {
			Bundle bundle = intent.getExtras();
			
			if((bundle != null) && (bundle.containsKey(PARAM_SCAN_INTERVAL)))
				mScanInterval = bundle.getInt(PARAM_SCAN_INTERVAL);		
		}
		
		this.mWifiScanManager = new WifiScanManager(this, this.mScanInterval);
		
		startLogging();
		
		Log.d(TAG, "Service Starting...");
		
		return START_STICKY;
	}
	
	public boolean isLogging() {
		return mIsLogging;
	}
	
	public void startLogging() {
		if(mIsLogging)
			return;
		
		mWifiScanManager.start();
		mIsLogging = true;
		
		showNotification("Logger service running");
	}
	
	public void stopLogging() {
		if(!mIsLogging)
			return;
		
		mWifiScanManager.stop();
		mIsLogging = false;
		
		cancelNotification();
	}
}