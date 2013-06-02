package com.blueodin.wifilogger.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blueodin.wifilogger.MainActivity;
import com.blueodin.wifilogger.R;
import com.blueodin.wifilogger.data.NetworkEntry;
import com.blueodin.wifilogger.fragments.lists.ImportListFragment;
import com.blueodin.wifilogger.importer.ImportManager;
import com.blueodin.wifilogger.importer.wigle.WigleImportManager;
import com.blueodin.wifilogger.importer.wigle.WigleLocation;
import com.blueodin.wifilogger.importer.wigle.WigleNetwork;

import java.util.HashMap;
import java.util.List;

public class ImportDetailFragment extends SectionDetailFragment {
	private RadioGroup mRadioGroup;
	private StatusListAdapter mStatusListAdapter;
	private ProgressBar mImportProgress;
	
	public ImportDetailFragment() { }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_import_detail, container, false);
	
		mRadioGroup = (RadioGroup)rootView.findViewById(R.id.radiogroup_import);
		
		mStatusListAdapter = new StatusListAdapter(getActivity());
		ListView statusList = (ListView)rootView.findViewById(R.id.list_import_status);
		statusList.setAdapter(mStatusListAdapter);
		
		mImportProgress = (ProgressBar)rootView.findViewById(R.id.progress_import_task);
				
		((Button)rootView.findViewById(R.id.button_import)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(mRadioGroup.getCheckedRadioButtonId()) {
				case R.id.radio_import_wigle:
					beginWigleImport();
					break;
				case R.id.radio_import_wardrive:
					beginWardriveImport();
					break;
				}
			}
		});
		
		return rootView;
	}
	
	protected void beginWigleImport() {
		mStatusListAdapter.add("Started import of WigleWifi database...");
		
		final WigleImportManager wigleImportManager = new WigleImportManager();
		wigleImportManager.registerResultsCallback(new ImportManager.OnResultsLoaded() {
			@Override
			public void onResultsLoaded() {
				HashMap<WigleNetwork, List<WigleLocation>> entryMap = wigleImportManager.getResults().getEntryMap();
				mStatusListAdapter.add(String.format("Loaded %d networks and %d locations from the WigleWifi database.", entryMap.size(), entryMap.values().size()));
				
				ImportListFragment f = (ImportListFragment) getFragmentManager().findFragmentByTag(MainActivity.TAG_NETWORK_LIST);
				if((f == null) || (!(f instanceof ImportListFragment))) {
					mStatusListAdapter.add("Unable to find network list fragment to update!");
					return;
				}
				
				f.updateNetworks(wigleImportManager.getResults().getNetworkEntryMap());
			}
			
			@Override
			public void onResultsProgress(int progress) {
				mImportProgress.setProgress(progress);
			}
		});
		
		wigleImportManager.parseDatabase();
	}
	
	protected void beginWardriveImport() {
		Toast.makeText(getActivity(), "Importing Wardrive database not yet supported.", Toast.LENGTH_SHORT).show();
	}
	
	private class StatusListAdapter extends ArrayAdapter<String> {
		private SparseArray<String> mTimestamp = new SparseArray<String>();
		
		public StatusListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_2, android.R.id.text1);
		}
		
		@Override
		public void add(String value) {
			mTimestamp.append(getCount(), DateFormat.format("MMM dd, yyyy h:mmaa", System.currentTimeMillis()).toString());
			super.add(value);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rootView = super.getView(position, convertView, parent);
			
			((TextView)rootView.findViewById(android.R.id.text2)).setText(mTimestamp.get(position));
			
			return rootView;
		}
	}

	@Override
	public void onNetworkSelected(NetworkEntry networkEntry) {
		
	}

	@Override
	public void onSectionSelected(NetworkEntry networkEntry) {
		
	}
	
	@Override
	public String getTitle() {
		return "Import Data";
	}
}
