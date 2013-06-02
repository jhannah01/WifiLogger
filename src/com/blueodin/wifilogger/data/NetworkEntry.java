package com.blueodin.wifilogger.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkEntry  {
	public final String bssid;
	public final String ssid;
	public final int frequency;
	public final String capabilities;
	public long lasttime;
	
	public NetworkEntry(String bssid, String ssid, int frequency, String capabilities) {
		this(bssid, ssid, frequency, capabilities, System.currentTimeMillis());
	}
	
	public NetworkEntry(String bssid, String ssid, int frequency, String capabilities, long lasttime) {
		this.bssid = bssid;
		this.ssid = ssid;
		this.frequency = frequency;
		this.capabilities = capabilities;
		this.lasttime = lasttime;
	}

	protected NetworkEntry(Cursor data) {
		this.bssid = data.getString(data.getColumnIndex(WifiDataContract.Network.Columns.BSSID));
		this.ssid = data.getString(data.getColumnIndex(WifiDataContract.Network.Columns.SSID));
		this.frequency = data.getInt(data.getColumnIndex(WifiDataContract.Network.Columns.FREQUENCY));
		this.capabilities = data.getString(data.getColumnIndex(WifiDataContract.Network.Columns.CAPABILITIES));
		this.lasttime = data.getLong(data.getColumnIndex(WifiDataContract.Network.Columns.LASTTIME));
	}
	
	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		
		values.put(WifiDataContract.Network.Columns.BSSID, this.bssid);
		values.put(WifiDataContract.Network.Columns.SSID, this.ssid);
		values.put(WifiDataContract.Network.Columns.FREQUENCY, this.frequency);
		values.put(WifiDataContract.Network.Columns.CAPABILITIES, this.capabilities);
		
		return values;
	}
	
	public String getFormattedCapabilities(boolean fullCrypto) {
		List<String> securities = new ArrayList<String>();
		Matcher m = Pattern.compile("(\\[(.*?)\\])+?").matcher(this.capabilities);
		while(m.find()) {
			String sec = m.group(2);
			if(!fullCrypto) {
				if(sec.contains("-"))
					sec = sec.split("-")[0];

				if(securities.contains(sec))
					continue;
			} else {
				if(sec.contains("-"))
					sec = String.format("%s (%s)", sec.substring(0, sec.indexOf("-")), sec.substring(sec.indexOf("-")+1).replace('-', ' '));
			}
			securities.add(sec);
		}

		return TextUtils.join(", ", securities);
	}
	
	public static void sortList(List<NetworkEntry> networks) {
		Collections.sort(networks, new Comparator<NetworkEntry>() {
			@Override
			public int compare(NetworkEntry lhs, NetworkEntry rhs) {
				if(lhs.lasttime < rhs.lasttime)
					return 1;
				
				if(lhs.lasttime > rhs.lasttime)
					return -1;
				
				return 0;
			}
		});
	}
	
	public String getFormattedLastTime() {
		return DateFormat.format("MMM dd, yyyy h:mmaa", this.lasttime).toString();
	}
	
	public String getRelativeLastTime() {
		return DateUtils.getRelativeTimeSpanString(this.lasttime).toString();
	}
	
	@Override
	public String toString() {
		return String.format("%s [%s] - %d MHz (%s)", this.ssid, this.bssid, this.frequency, getFormattedCapabilities(false));
	}
}