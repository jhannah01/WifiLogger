package com.blueodin.wifilogger.fragments.lists;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.blueodin.wifilogger.MainActivity;
import com.blueodin.wifilogger.R;
import com.blueodin.wifilogger.data.NetworkEntry;
import com.blueodin.wifilogger.data.WirelessNetworkEntries;

import java.util.ArrayList;
import java.util.List;

public class MultiSelectNetworkListFragment extends ListFragment implements NetworkListInterface {
	private static final String ARG_SELECTED_ITEMS = "arg_selected_items";
	private MultiSelectNetworkAdapter mListAdapter;
	private WirelessNetworkEntries mNetworkEntries;
	private List<Integer> mSelectedItems = new ArrayList<Integer>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mNetworkEntries = ((MainActivity)getActivity()).getNetworkEntries();
		mListAdapter = new MultiSelectNetworkAdapter(getActivity());
		
		setListAdapter(mListAdapter);
		
		ListView networkList = getListView();
		
		networkList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		networkList.setSelector(R.drawable.listitem_selected);
		
		networkList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mSelectedItems.add(position);
				mListAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				mSelectedItems.clear();
				mListAdapter.notifyDataSetChanged();
			}
		});
		
		if((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_SELECTED_ITEMS))) {
			mSelectedItems = savedInstanceState.getIntegerArrayList(ARG_SELECTED_ITEMS);
			for(int item : mSelectedItems)
				networkList.setSelection(item);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putIntegerArrayList(ARG_SELECTED_ITEMS, (ArrayList<Integer>)mSelectedItems);
	}
	
	private class MultiSelectNetworkAdapter extends ArrayAdapter<NetworkEntry> {
		private final LayoutInflater mLayoutInflater;
		public MultiSelectNetworkAdapter(Context context) {
			super(context, android.R.id.text1);
			mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			NetworkEntry network = getItem(position);
			
			View rowView = mLayoutInflater.inflate(R.layout.network_row, parent, false);
			
			if(mSelectedItems.contains(position))
				rowView.setBackgroundResource(R.color.list_header_background);
			
			((TextView)rowView.findViewById(R.id.text_network_row_bssid)).setText(network.bssid);
			((TextView)rowView.findViewById(R.id.text_network_row_ssid)).setText(network.ssid);
			((TextView)rowView.findViewById(R.id.text_network_row_lastseen)).setText(network.getFormattedLastTime());
			((TextView)rowView.findViewById(R.id.text_network_row_locations)).setText(String.format("#%d", mNetworkEntries.getLocations(network).size()));
			
			return rowView;
		}
	}

	public List<NetworkEntry> getSelectedNetworks() {
		List<NetworkEntry> networks = new ArrayList<NetworkEntry>();
		
		for(int item : mSelectedItems)
			networks.add(mListAdapter.getItem(item));
		
		return networks;
	}

	@Override
	public NetworkEntry getSelectedNetwork() {
		if(mSelectedItems.size() == 0)
			return null;
		
		return getSelectedNetworks().get(0);
	}

	@Override
	public void updateNetworks() {
		mListAdapter.clear();
		
		for(NetworkEntry network : mNetworkEntries.getNetworks())
			mListAdapter.add(network);
		
		mListAdapter.notifyDataSetChanged();
	}
}
