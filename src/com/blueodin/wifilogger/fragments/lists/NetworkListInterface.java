package com.blueodin.wifilogger.fragments.lists;

import com.blueodin.wifilogger.data.NetworkEntry;

public interface NetworkListInterface {
	public NetworkEntry getSelectedNetwork();
	public void updateNetworks();
}
