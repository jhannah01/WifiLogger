package com.blueodin.wifilogger.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.blueodin.wifilogger.MainActivity;
import com.blueodin.wifilogger.R;
import com.blueodin.wifilogger.data.NetworkEntry;
import com.blueodin.wifilogger.data.WirelessNetworkEntries;


public class MapSectionFragment extends SectionDetailFragment {
	private static final String ARG_NETWORK_BSSID = "arg_network_bssid";

	private TextView mMapTextView;
	
	private WirelessNetworkEntries mNetworkEntries;
	private NetworkEntry mNetwork = null;

	public MapSectionFragment() { }
	
	public static MapSectionFragment newInstance(NetworkEntry network) {
		if(network == null)
			return new MapSectionFragment();
		
		return newInstance(network.bssid);
	}
	
	public static MapSectionFragment newInstance(String bssid) {
		MapSectionFragment f = new MapSectionFragment();
		Bundle args = new Bundle();
		args.putString(ARG_NETWORK_BSSID, bssid);
		f.setArguments(args);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mNetworkEntries = ((MainActivity)getActivity()).getNetworkEntries();
		
		String bssid = "";

		Bundle arguments = getArguments();
		if((arguments != null) && (arguments.containsKey(ARG_NETWORK_BSSID)))
			bssid = arguments.getString(ARG_NETWORK_BSSID);
		
		if((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_NETWORK_BSSID)))
			bssid = savedInstanceState.getString(ARG_NETWORK_BSSID);
				
		if(!bssid.isEmpty())
			mNetwork = mNetworkEntries.getNetwork(bssid);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(mNetwork != null)
			outState.putString(ARG_NETWORK_BSSID, mNetwork.bssid);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		int horizMargin = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
		int vertMargin = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
		
		FrameLayout frameLayout = new FrameLayout(getActivity());
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		frameLayout.setLayoutParams(layoutParams);
		frameLayout.setPadding(horizMargin, vertMargin, horizMargin, vertMargin);

		mMapTextView = new TextView(getActivity());
		onNetworkSelected(mNetwork);

		frameLayout.addView(mMapTextView);

		return frameLayout;
	}

	@Override
	public void onNetworkSelected(NetworkEntry networkEntry) {
		if (networkEntry == null)
			mMapTextView.setText("No Network Selected");
		else
			mMapTextView.setText(String.format(
					"Map of Selected Network: %s goes here",
					networkEntry.toString()));
	}

	@Override
	public void onSectionSelected(NetworkEntry networkEntry) {
		
	}
	
	@Override
	public String getTitle() {
		return "Maps";
	}
}

