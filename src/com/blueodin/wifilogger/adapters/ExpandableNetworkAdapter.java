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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExpandableNetworkAdapter extends BaseExpandableListAdapter {
	private List<Group> mNetworks = new ArrayList<Group>();
	private Context mContext;
	private LayoutInflater mInflater;
	private boolean mLimitLocations = true;

	public class Group {
		private NetworkEntry mNetwork;
		private List<LocationEntry> mLocations;

		public Group(NetworkEntry network,
				WirelessNetworkEntries networkEntries) {
			this(network, networkEntries.getLocations(network.bssid));
		}

		public Group(NetworkEntry network, List<LocationEntry> locations) {
			mNetwork = network;
			
			Collections.sort(locations, new Comparator<LocationEntry>() {
				@Override
				public int compare(LocationEntry lhs, LocationEntry rhs) {
					if (lhs.timestamp > rhs.timestamp)
						return -1;
					else if (lhs.timestamp < rhs.timestamp)
						return 1;
					return 0;
				}
			});

			mLocations = locations;
		}

		public NetworkEntry getNetwork() {
			return mNetwork;
		}

		public List<LocationEntry> getLocations() {
			if (mLimitLocations)
				return mLocations.subList(0, Math.min(15, mLocations.size()));

			return mLocations;
		}

		public LocationEntry getLocation(int index) {
			return mLocations.get(index);
		}

		public int getLocationCount() {
			if (mLimitLocations)
				return Math.min(15, mLocations.size());

			return mLocations.size();
		}

		public boolean areLocationsLimited() {
			return mLimitLocations;
		}

		public View getGroupView(boolean isExpanded, View convertView,
				ViewGroup parent) {
			View groupView = mInflater.inflate(R.layout.network_row, parent,
					false);

			((TextView) groupView.findViewById(R.id.text_network_row_bssid))
					.setText(mNetwork.bssid);
			((TextView) groupView.findViewById(R.id.text_network_row_ssid))
					.setText(mNetwork.ssid);
			((TextView) groupView.findViewById(R.id.text_network_row_lastseen))
					.setText(mNetwork.getFormattedLastTime());
			((TextView) groupView.findViewById(R.id.text_network_row_locations))
					.setText(String.format("#%d", getLocationCount()));
			
			return groupView;
		}

		public View getChildView(int childPosition, boolean isLastChild,
				View convertView, ViewGroup parent) {
			LocationEntry location = getLocation(childPosition);

			View childView = mInflater.inflate(R.layout.location_row, parent,
					false);

			int bgColor = mContext.getResources().getColor(
					((childPosition % 2) == 0) ? R.color.list_item_background
							: R.color.list_item_background_alt);

			childView.setBackgroundColor(bgColor);

			((TextView) childView.findViewById(R.id.text_location_level))
					.setText(String.format("%d dBm", location.level));
			((TextView) childView.findViewById(R.id.text_location_timestamp))
					.setText(location.getRelativeTimestamp());

			return childView;
		}
	}

	public ExpandableNetworkAdapter(Context context,
			WirelessNetworkEntries networkEntries) {
		this(context);
		update(networkEntries);
	}

	public ExpandableNetworkAdapter(Context context) {
		super();
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void update(WirelessNetworkEntries networkEntries) {
		mNetworks.clear();

		for (NetworkEntry entry : networkEntries.getNetworks())
			mNetworks.add(new Group(entry, networkEntries));

		notifyDataSetChanged();
	}

	@Override
	public int getGroupCount() {
		return mNetworks.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mNetworks.get(groupPosition).getLocationCount();
	}

	@Override
	public Group getGroup(int groupPosition) {
		return mNetworks.get(groupPosition);
	}

	public NetworkEntry getNetwork(int groupPosition) {
		return getGroup(groupPosition).getNetwork();
	}

	public List<LocationEntry> getLocations(int groupPosition) {
		return getGroup(groupPosition).getLocations();
	}

	@Override
	public LocationEntry getChild(int groupPosition, int childPosition) {
		return mNetworks.get(groupPosition).getLocation(childPosition);
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
		return mNetworks.get(groupPosition).getGroupView(isExpanded,
				convertView, parent);
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		return mNetworks.get(groupPosition).getChildView(childPosition,
				isLastChild, convertView, parent);
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		if (getGroup(groupPosition).areLocationsLimited()) {
			if (childPosition > 14)
				return false;
		}

		return true;
	}

	public void setLimitLocations(boolean value) {
		mLimitLocations = value;
		notifyDataSetChanged();
	}
}