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

public class OverviewSectionFragment extends SectionDetailFragment {
	private static final String ARG_NETWORK_BSSID = "arg_network_bssid";
	private TextView mOverviewTextView;
	private WirelessNetworkEntries mNetworkEntries;
	private NetworkEntry mNetwork = null;
	
	public OverviewSectionFragment() { }
	
	public static OverviewSectionFragment newInstance(NetworkEntry network) {
		if(network == null)
			return new OverviewSectionFragment();
		
		return newInstance(network.bssid);
	}
	
	public static OverviewSectionFragment newInstance(String bssid) {
		OverviewSectionFragment f = new OverviewSectionFragment();
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
		int horizMargin = getResources().getDimensionPixelSize(
				R.dimen.activity_horizontal_margin);
		int vertMargin = getResources().getDimensionPixelSize(
				R.dimen.activity_vertical_margin);
		FrameLayout frameLayout = new FrameLayout(getActivity());
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		frameLayout.setLayoutParams(layoutParams);
		frameLayout.setPadding(horizMargin, vertMargin, horizMargin,
				vertMargin);

		mOverviewTextView = new TextView(getActivity());
		onNetworkSelected(mNetwork);

		frameLayout.addView(mOverviewTextView);

		return frameLayout;
	}

	@Override
	public void onNetworkSelected(NetworkEntry networkEntry) {
		if (networkEntry == null)
			mOverviewTextView.setText("No Network Selected");
		else
			mOverviewTextView.setText(String.format(
					"Overview of selected Network: %s goes here",
					networkEntry.toString()));
	}

	@Override
	public void onSectionSelected(NetworkEntry networkEntry) {

	}

	@Override
	public String getTitle() {
		return "Overview";
	}
}