package com.blueodin.wifilogger.fragments.lists;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.blueodin.wifilogger.R;
import com.blueodin.wifilogger.adapters.NetworkAdapterList;
import com.blueodin.wifilogger.data.LocationEntry;
import com.blueodin.wifilogger.data.NetworkEntry;
import com.blueodin.wifilogger.importer.ImportManager.ImportResults;

import java.util.HashMap;
import java.util.List;

public class ImportListFragment extends Fragment implements NetworkListInterface {
	private static final String ARG_LAST_GROUP = "arg_last_group";
	
	private NetworkAdapterList mListAdapter;
	private int mLastGroupExpanded = ListView.INVALID_POSITION;

	private ExpandableListView mNetworkList;

	public ImportListFragment() { }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mListAdapter = new NetworkAdapterList(getActivity());
		
		if (savedInstanceState != null)
			mLastGroupExpanded = savedInstanceState.getInt(ARG_LAST_GROUP, ListView.INVALID_POSITION);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_LAST_GROUP, mLastGroupExpanded);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_import_list, container, false);
		
		mNetworkList = (ExpandableListView)rootView.findViewById(R.id.list_imported_networks);
		
		mNetworkList.setAdapter(mListAdapter);
		
		mNetworkList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				if (mLastGroupExpanded > 0)
					mNetworkList.collapseGroup(mLastGroupExpanded);
				
				mLastGroupExpanded = groupPosition;
			}
		});
		
		mNetworkList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(int groupPosition) {
				if (mLastGroupExpanded == groupPosition)
					mLastGroupExpanded = -1;
			}
		});
		
		if (mLastGroupExpanded != -1)
			mNetworkList.expandGroup(mLastGroupExpanded);

		return rootView;
	}
	
	public void updateNetworks(HashMap<NetworkEntry, List<LocationEntry>> entryMap) {
		mListAdapter.update(entryMap);
	}

	@Override
	public NetworkEntry getSelectedNetwork() {
		if(mLastGroupExpanded != ListView.INVALID_POSITION)
			return mListAdapter.getNetwork(mLastGroupExpanded);
		
		return null;
	}

	@Override
	public void updateNetworks() {
		
	}
}