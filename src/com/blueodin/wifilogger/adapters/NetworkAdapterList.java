package com.blueodin.wifilogger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.blueodin.wifilogger.R;
import com.blueodin.wifilogger.data.LocationEntry;
import com.blueodin.wifilogger.data.NetworkEntry;
import com.blueodin.wifilogger.data.WirelessNetworkEntries;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NetworkAdapterList extends BaseExpandableListAdapter {
	private List<ListEntry> mEntries = new ArrayList<ListEntry>();
	private LayoutInflater mLayoutInflater;
	
	public NetworkAdapterList(Context context) {
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void update(WirelessNetworkEntries networkEntries) {
		update(networkEntries.getEntityMap());
	}
	
	public void update(HashMap<NetworkEntry, List<LocationEntry>> entryMap) {
		mEntries.clear();
		
		List<NetworkEntry> networks = new ArrayList<NetworkEntry>(entryMap.keySet());
		NetworkEntry.sortList(networks);
		
		for(NetworkEntry network : networks)
			mEntries.add(new ListEntry(mLayoutInflater, network, entryMap.get(network)));
		
		notifyDataSetChanged();
	}
	
	@Override
	public int getGroupCount() {
		return mEntries.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(getGroup(groupPosition) != null)
			return 1;
		
		return 0;
	}

	@Override
	public ListEntry getGroup(int groupPosition) {
		return mEntries.get(groupPosition);
	}
	
	public NetworkEntry getNetwork(int groupPosition) {
		return getGroup(groupPosition).getNetwork();
	}

	@Override
	public List<LocationEntry> getChild(int groupPosition, int childPosition) {
		return getGroup(groupPosition).getLocations();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		return getGroup(groupPosition).getView(convertView, parent);
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		return getGroup(groupPosition).getChildView(convertView, parent);
		
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public static class ListEntry {
		private LayoutInflater mLayoutInflater;
		private NetworkEntry mNetwork;
		private List<LocationEntry> mLocations;
		
		public ListEntry(LayoutInflater layoutInflater, NetworkEntry network, List<LocationEntry> locations) {
			mLayoutInflater = layoutInflater;
			mNetwork = network;
			mLocations = locations;
		}
		
		public NetworkEntry getNetwork() {
			return mNetwork;
		}
		
		public List<LocationEntry> getLocations() {
			return mLocations;
		}
		
		public int getAverageLevel() {
			if(mLocations.size() == 0)
				return 0;
			
			int i = 0;
			
			for(LocationEntry location : mLocations)
				i += location.level;
			
			i /= mLocations.size();
			
			return i;
		}
		
		public View getView(View convertView, ViewGroup parent) {
			View view = mLayoutInflater.inflate(R.layout.network_row, parent, false);
			
			((TextView)view.findViewById(R.id.text_network_row_bssid)).setText(mNetwork.bssid);
			((TextView)view.findViewById(R.id.text_network_row_ssid)).setText(mNetwork.ssid);
			((TextView)view.findViewById(R.id.text_network_row_lastseen)).setText(mNetwork.getFormattedLastTime());
			((TextView)view.findViewById(R.id.text_network_row_locations)).setText(String.format("#%d", getLocations().size()));
	
			return view;
		}
		
		public View getChildView(View convertView, ViewGroup parent) {
			View view = mLayoutInflater.inflate(R.layout.network_child, parent, false);
			
			((TextView)view.findViewById(R.id.text_nework_child_avglevel)).setText(String.format("%d dBm", getAverageLevel()));
			((TextView)view.findViewById(R.id.text_nework_child_count)).setText(String.format("#%d", mLocations.size()));
			((TextView)view.findViewById(R.id.text_nework_child_frequency)).setText(String.format("%d MHz", mNetwork.frequency));
			((TextView)view.findViewById(R.id.text_nework_child_capabilities)).setText(mNetwork.getFormattedCapabilities(true));

			return view;
		}
	}
}
