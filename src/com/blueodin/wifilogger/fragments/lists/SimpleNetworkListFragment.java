package com.blueodin.wifilogger.fragments.lists;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.blueodin.wifilogger.MainActivity;
import com.blueodin.wifilogger.R;
import com.blueodin.wifilogger.data.NetworkEntry;
import com.blueodin.wifilogger.data.WirelessNetworkEntries;

import java.util.List;

public class SimpleNetworkListFragment extends ListFragment {
	private OnNetworkSelectedListener mListener;
	private NetworkListAdapter mAdapter;
	private WirelessNetworkEntries mNetworkEntries;

	public SimpleNetworkListFragment() { }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mNetworkEntries = ((MainActivity)getActivity()).getNetworkEntries();
		mAdapter = new NetworkListAdapter(getActivity());
		
		setListAdapter(mAdapter);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnNetworkSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnNetworkSelectedListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		if (null != mListener)
			mListener.onNetworkSelected(mAdapter.getItem(position));
	}

	public interface OnNetworkSelectedListener {
		public void onNetworkSelected(NetworkEntry network);
	}

	private class NetworkListAdapter extends ArrayAdapter<NetworkEntry> {
		private LayoutInflater mInflater;
	
		public NetworkListAdapter(Context context) {
			super(context, R.layout.network_row);
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			for(NetworkEntry network : mNetworkEntries.getNetworks())
				add(network);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			NetworkEntry network = getItem(position);
			
			View row = mInflater.inflate(R.layout.network_row, parent, false);
			
			((TextView)row.findViewById(R.id.text_network_row_bssid)).setText(network.bssid);
			((TextView)row.findViewById(R.id.text_network_row_ssid)).setText(network.ssid);
			((TextView)row.findViewById(R.id.text_network_row_lastseen)).setText(network.getFormattedLastTime());
			((TextView)row.findViewById(R.id.text_network_row_locations)).setText(String.format("#%d", mNetworkEntries.getLocations(network).size()));
	
			return row;
		}
	}
	
	public void updateNetworks(List<NetworkEntry> networks) {
		mAdapter.clear();
		mAdapter.addAll(networks);
		mAdapter.notifyDataSetChanged();
	}
}
