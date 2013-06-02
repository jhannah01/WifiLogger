package com.blueodin.wifilogger.fragments;

import android.support.v4.app.Fragment;

import com.blueodin.wifilogger.data.NetworkEntry;

public abstract class SectionDetailFragment extends Fragment implements OnSectionDetailCallback {
	public SectionDetailFragment() { }
	
	public abstract void onNetworkSelected(NetworkEntry networkEntry);	
	public abstract void onSectionSelected(NetworkEntry networkEntry);
	public abstract String getTitle();
}
