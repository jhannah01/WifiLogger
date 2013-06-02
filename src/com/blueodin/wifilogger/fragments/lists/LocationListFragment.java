package com.blueodin.wifilogger.fragments.lists;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.blueodin.wifilogger.MainActivity;
import com.blueodin.wifilogger.R;
import com.blueodin.wifilogger.data.LocationEntry;
import com.blueodin.wifilogger.data.NetworkEntry;
import com.blueodin.wifilogger.data.WirelessNetworkEntries;

import java.util.List;

public class LocationListFragment extends ListFragment {
	public static final String ARG_NETWORK_BSSID = "network_bssid";
	
	private LocationListAdapter mAdapter;

	private WirelessNetworkEntries mNetworkEntries;
	
	public static LocationListFragment newInstance(NetworkEntry network) {
		return newInstance(network.bssid);
	}
	
	public static LocationListFragment newInstance(String bssid) {
		Bundle args = new Bundle();
		args.putString(ARG_NETWORK_BSSID, bssid);
		LocationListFragment f = new LocationListFragment();
		f.setArguments(args);
		return f;
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String bssid = "";
	
		mNetworkEntries = ((MainActivity)getActivity()).getNetworkEntries();
		
		Bundle args = getArguments();
		if((args != null) && (args.containsKey(ARG_NETWORK_BSSID)))
			bssid = args.getString(ARG_NETWORK_BSSID);
		
		if((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_NETWORK_BSSID)))
			bssid = savedInstanceState.getString(ARG_NETWORK_BSSID);
		
		mAdapter = new LocationListAdapter(getActivity());
		
		if(!bssid.isEmpty())
			updateLocations(mNetworkEntries.getLocations(bssid));
		
		setListAdapter(mAdapter);
	}
	
	public void updateLocations(List<LocationEntry> locations) {
		mAdapter.clear();
		
		for(LocationEntry location : locations)
			mAdapter.add(location);
		
		mAdapter.notifyDataSetChanged();
	}
	
	private class LocationListAdapter extends ArrayAdapter<LocationEntry> {
		private LayoutInflater mInflater;

		public LocationListAdapter(Context context) {
			super(context, R.layout.location_row);
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LocationEntry location = getItem(position);
			
			View row = mInflater.inflate(R.layout.location_row, parent, false);
			
			((TextView)row.findViewById(R.id.text_location_level)).setText(String.format("%d dBm", location.level));
			((TextView)row.findViewById(R.id.text_location_timestamp)).setText(location.getFormattedTimestamp());
			
			return row;
		}
	}
}
