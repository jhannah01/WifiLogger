package com.blueodin.wifilogger.importer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.AsyncTask;
import com.blueodin.wifilogger.data.LocationEntry;
import com.blueodin.wifilogger.data.NetworkEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ImportManager<N extends ImportedNetwork, L extends ImportedLocation> {
	private ImportResults mResults = null;
	private List<OnResultsLoaded> mCallbacks = new ArrayList<OnResultsLoaded>();
	
	public ImportManager() {
		
	}
	
	public ImportResults getResults() {
		return mResults;
	}
	
	public void parseDatabase() {
		new ImportTask().execute();
	}
	
	public class ImportResults {
		private HashMap<N, List<L>> mEntryMap = new HashMap<N, List<L>>();
		private HashMap<String, N> mNetworkMap = new HashMap<String, N>();
		private HashMap<String, List<L>> mLocationsMap = new HashMap<String, List<L>>();
		
		public ImportResults(HashMap<String, N> networkMap, HashMap<String, List<L>> locationsMap) {
			mNetworkMap = networkMap;
			mLocationsMap = locationsMap;
			
			for(Map.Entry<String, N> entry : networkMap.entrySet())
				mEntryMap.put(entry.getValue(), new ArrayList<L>());
			
			for(Map.Entry<String, List<L>> entry : locationsMap.entrySet()) {
				if(!networkMap.containsKey(entry.getKey()))
					continue;
				mEntryMap.put(networkMap.get(entry.getKey()), entry.getValue());
			}
		}
		
		public HashMap<N, List<L>> getEntryMap() {
			return mEntryMap;
		}
		
		public HashMap<String, N> getNetworkMap() {
			return mNetworkMap;
		}
		
		public HashMap<String, List<L>> getLocationsMap() {
			return mLocationsMap;
		}
		
		public HashMap<NetworkEntry, List<LocationEntry>> getNetworkEntryMap() {
			HashMap<NetworkEntry, List<LocationEntry>> entryMap = new HashMap<NetworkEntry, List<LocationEntry>>();
			
			for(Map.Entry<N, List<L>> entry : mEntryMap.entrySet()) {
				N importedNetwork = entry.getKey();
				NetworkEntry network = new NetworkEntry(importedNetwork.bssid, importedNetwork.ssid, importedNetwork.frequency, importedNetwork.capabilities, importedNetwork.lasttime);
				
				List<LocationEntry> locations = new ArrayList<LocationEntry>();
				
				for(L location : entry.getValue())
					locations.add(new LocationEntry(location.bssid, network.ssid, location.level, location.timestamp));
				
				entryMap.put(network, locations);
			}
			
			return entryMap;
		}
	}
	
	public interface OnResultsLoaded {
		public void onResultsLoaded();
		public void onResultsProgress(int progress);
	}
	
	public void registerResultsCallback(OnResultsLoaded callback) {
		mCallbacks .add(callback);
	}
	
	public void unregisterResultsCallback(OnResultsLoaded callback) {
		mCallbacks.remove(callback);
	}
	
	public abstract String getNetworksTableName();
	public abstract String getLocationsTableName();
	public abstract String getDatabasePath();
	
	protected abstract N getNetwork(Cursor networkData);
	protected abstract L getLocation(Cursor locationData);

	protected String getNetworkQuerySelection() {
		return null;
	}
	
	protected String getLocationQuerySelection() {
		return null;
	}
	
	protected String getNetworkOrderBy() {
		return null;
	}
	
	protected String getLocationOrderBy() {
		return null;
	}
	
	public class ImportTask extends AsyncTask<Void, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			HashMap<String, N> networkMap = new HashMap<String, N>();
			HashMap<String, List<L>> locationsMap = new HashMap<String, List<L>>();
			
			SQLiteDatabase db = null;
			Cursor networkData = null;
			Cursor locationData = null;
			try {
				db = SQLiteDatabase.openDatabase(getDatabasePath(), null, SQLiteDatabase.OPEN_READONLY);
			
				SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
				queryBuilder.setTables(getNetworksTableName());

				networkData = queryBuilder.query(db, null, getNetworkQuerySelection(), null, null, null, getNetworkOrderBy());
			
				queryBuilder = new SQLiteQueryBuilder();
				queryBuilder.setTables(getLocationsTableName());
				
				locationData = queryBuilder.query(db, null, getLocationQuerySelection(), null, null, null, getLocationOrderBy());
				
				if(!networkData.moveToFirst())
					return false;
				
				int networkCount = networkData.getCount();
				
				if(!locationData.moveToFirst())
					return false;
				
				int locationCount = locationData.getCount();
				
				int c = networkCount + locationCount;
				double i = 0;
				
				while(!networkData.isAfterLast()) {
					N network = getNetwork(networkData);
					networkMap.put(network.bssid, network);
					
					publishProgress((int)((i/c)*100));
					i++;
					
					networkData.moveToNext();
				}
				
				while(!locationData.isAfterLast()) {
					L location = getLocation(locationData);
					
					if(!locationsMap.containsKey(location.bssid))
						locationsMap.put(location.bssid, new ArrayList<L>());
					
					locationsMap.get(location.bssid).add(location);

					
					publishProgress((int)((i/c)*100));
					i++;
					
					locationData.moveToNext();
				}
			} finally {
				if(networkData != null)
					networkData.close();
				
				if(locationData != null)
					locationData.close();
				
				if(db != null)
					db.close();
			}
			
			mResults = new ImportResults(networkMap, locationsMap);
			
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			for(OnResultsLoaded callback : mCallbacks)
				callback.onResultsProgress(values[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			if(!success)
				return;
			
			for(OnResultsLoaded callback : mCallbacks)
				callback.onResultsLoaded();
		}
	}
}
