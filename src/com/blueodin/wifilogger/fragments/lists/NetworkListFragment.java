package com.blueodin.wifilogger.fragments.lists;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.blueodin.wifilogger.MainActivity;
import com.blueodin.wifilogger.R;
import com.blueodin.wifilogger.adapters.NetworkAdapterList;
import com.blueodin.wifilogger.data.NetworkEntry;
import com.blueodin.wifilogger.data.WirelessNetworkEntries;

public class NetworkListFragment extends Fragment implements NetworkListInterface {
	private static final String ARG_LAST_GROUP = "arg_last_group";
	private NetworkAdapterList mListAdapter;
	private OnNetworkSelectedCallback mCallback;
	private int mLastGroupExpanded = -1;
	private WirelessNetworkEntries mNetworkEntries;

	public interface OnNetworkSelectedCallback {
		public void onNetworkSelected(NetworkEntry network);
	}

	public NetworkListFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mListAdapter = new NetworkAdapterList(getActivity());
		mNetworkEntries = ((MainActivity)getActivity()).getNetworkEntries();
		
		if (savedInstanceState != null)
			mLastGroupExpanded = savedInstanceState.getInt(ARG_LAST_GROUP, -1);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_LAST_GROUP, mLastGroupExpanded);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof OnNetworkSelectedCallback) {
			mCallback = (OnNetworkSelectedCallback) activity;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_network_list, container,
				false);

		final ExpandableListView listNetworks = (ExpandableListView) view
				.findViewById(R.id.list_networks);

		listNetworks.setAdapter(mListAdapter);

		listNetworks
				.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
					@Override
					public void onGroupExpand(int groupPosition) {
						if (mCallback != null)
							mCallback.onNetworkSelected(mListAdapter.getNetwork(groupPosition));
						
						if (mLastGroupExpanded > 0)
							listNetworks.collapseGroup(mLastGroupExpanded);
						
						mLastGroupExpanded = groupPosition;
					}
				});

		listNetworks
				.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
					@Override
					public void onGroupCollapse(int groupPosition) {
						if ((mCallback != null) && (mLastGroupExpanded != groupPosition))
							mCallback.onNetworkSelected(null);
						
						if (mLastGroupExpanded == groupPosition)
							mLastGroupExpanded = -1;
					}
				});

		if (mLastGroupExpanded != -1)
			listNetworks.expandGroup(mLastGroupExpanded);

		return view;
	}

	@Override
	public NetworkEntry getSelectedNetwork() {
		if(mLastGroupExpanded == -1)
			return null;
		
		return mListAdapter.getGroup(mLastGroupExpanded).getNetwork();
	}

	@Override
	public void updateNetworks() {
		mListAdapter.update(mNetworkEntries);	
	}
}
