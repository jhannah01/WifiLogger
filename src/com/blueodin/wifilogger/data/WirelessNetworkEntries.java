package com.blueodin.wifilogger.data;

import android.content.Context;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WirelessNetworkEntries {
	private HashMap<String, NetworkEntry> mNetworkMap = new HashMap<String, NetworkEntry>();
	private HashMap<String, List<LocationEntry>> mLocationsMap = new HashMap<String, List<LocationEntry>>();
	
	private final Context mContext;
	
	public WirelessNetworkEntries(Context context) {
		mContext = context;
	}
	
	public void loadEntries() {
		loadNetworks();
		loadLocations();
	}
	
	public boolean loadNetworks() {
		Cursor data = mContext.getContentResolver().query(WifiDataContract.Network.CONTENT_URI, null, null, null, null);
		boolean result = loadNetworks(data);
		data.close();
		return result;
	}
	
	public boolean loadNetworks(Cursor data) {
		mNetworkMap.clear();
		
		if(!data.moveToFirst()) {
			data.close();
			return false;
		}
		
		while(!data.isAfterLast()) {
			NetworkEntry network = new NetworkEntry(data);
			mNetworkMap.put(network.bssid, network);
			data.moveToNext();
		}
		
		data.close();
		
		return true;
	}
	
	public boolean loadLocations() {
		Cursor data = mContext.getContentResolver().query(WifiDataContract.Location.CONTENT_URI, null, null, null, null);
		
		boolean result = loadLocations(data);
		
		data.close();
		
		return result;
	}
	
	public boolean loadLocations(Cursor data) {
		mLocationsMap.clear();
				
		if(!data.moveToFirst()) {
			data.close();
			return false;
		}
		
		while(!data.isAfterLast()) {
			LocationEntry location = new LocationEntry(data);
			
			if(!mLocationsMap.containsKey(location.bssid))
				mLocationsMap.put(location.bssid, new ArrayList<LocationEntry>());
			
			mLocationsMap.get(location.bssid).add(location);
			
			data.moveToNext();
		}
		
		data.close();
		
		return true;
	}
	
	public String add(ScanResult result) {
		boolean hasNetworkEntry = mNetworkMap.containsKey(result.BSSID);
		
		if(!hasNetworkEntry) {
			NetworkEntry networkEntry = new NetworkEntry(result.BSSID, result.SSID, result.frequency, result.capabilities);
			mNetworkMap.put(result.BSSID, networkEntry);
			mContext.getContentResolver().insert(WifiDataContract.Network.CONTENT_URI, networkEntry.getContentValues());
		}
		
		LocationEntry locationEntry = new LocationEntry(result.BSSID, result.SSID, result.level, System.currentTimeMillis());
		
		if(!mLocationsMap.containsKey(result.BSSID))
			mLocationsMap.put(result.BSSID, new ArrayList<LocationEntry>());
		
		mLocationsMap.get(result.BSSID).add(locationEntry);
		
		mContext.getContentResolver().insert(WifiDataContract.Location.CONTENT_URI, locationEntry.getContentValues());
		
		if(hasNetworkEntry)
			mNetworkMap.get(result.BSSID).lasttime = locationEntry.timestamp;
		
		return result.BSSID;
	}
	
	public HashMap<String, NetworkEntry> getNetworkMap() {
		return mNetworkMap;
	}
	
	public HashMap<String, List<LocationEntry>> getLocationsMap() {
		return mLocationsMap;
	}
	
	public List<NetworkEntry> getNetworks() {
		return new ArrayList<NetworkEntry>(mNetworkMap.values());
	}
	
	public List<LocationEntry> getLocations() {
		List<LocationEntry> results = new ArrayList<LocationEntry>();
		
		for(List<LocationEntry> locations : mLocationsMap.values())
			results.addAll(locations);
		
		return results;
	}
	
	public List<LocationEntry> getLocations(NetworkEntry network) {
		if(network == null)
			return new ArrayList<LocationEntry>();
		
		return getLocations(network.bssid);
	}
	
	public List<LocationEntry> getLocations(String bssid) {
		if(!mLocationsMap.containsKey(bssid))
			return new ArrayList<LocationEntry>();
		
		return mLocationsMap.get(bssid);
	}
	
	public NetworkEntry getNetwork(String bssid) {
		return mNetworkMap.get(bssid);
	}

	public void clear() {
		mNetworkMap.clear();
		mLocationsMap.clear();
	}
	
	public interface OnNetworkEntriesLoaded {
		public void onNetworkEntriesLoaded(WirelessNetworkEntries networkEntries);
	}
	
	public static class RecordsLoader extends AsyncTaskLoader<WirelessNetworkEntries> {
		private WirelessNetworkEntries mNetworkEntries = null;
		
		public RecordsLoader(Context context) {
			super(context);
		}
		
		@Override
		public WirelessNetworkEntries loadInBackground() {
			if(mNetworkEntries == null)
				mNetworkEntries = new WirelessNetworkEntries(getContext());
			
			mNetworkEntries.loadEntries(); 
			
			return mNetworkEntries;
		}
		
		@Override
		protected void onStartLoading() {
			super.onStartLoading();
			
			if(mNetworkEntries != null)
				deliverResult(mNetworkEntries);
			
			if(takeContentChanged() || (mNetworkEntries == null))
				forceLoad();
		}
		
		@Override
		protected void onStopLoading() {
			super.onStopLoading();
			cancelLoad();
		}
		
		@Override
		protected void onReset() {
			super.onReset();
			
			onStopLoading();
			
			if(mNetworkEntries != null)
				mNetworkEntries = null;
		}
	}

	public HashMap<NetworkEntry, List<LocationEntry>> getEntityMap() {
		HashMap<NetworkEntry, List<LocationEntry>> entityMap = new HashMap<NetworkEntry, List<LocationEntry>>();
		
		for(Map.Entry<String, NetworkEntry> entry : mNetworkMap.entrySet())
			entityMap.put(entry.getValue(), mLocationsMap.get(entry.getKey()));
		
		return entityMap;
	}
}