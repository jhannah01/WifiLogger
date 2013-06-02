package com.blueodin.wifilogger.importer.wigle;

import android.database.Cursor;
import android.os.Environment;
import com.blueodin.wifilogger.importer.ImportManager;

public class WigleImportManager extends ImportManager<WigleNetwork,WigleLocation> {
	@Override
	public String getDatabasePath() {
		return Environment.getExternalStorageDirectory() + "/wiglewifi/wiglewifi.sqlite";
	}
	
	@Override
	public String getNetworksTableName() {
		return "network";
	}

	@Override
	public String getLocationsTableName() {
		return "location";
	}
	
	@Override
	protected WigleNetwork getNetwork(Cursor networkData) {
		return new WigleNetwork(networkData);
	}
	
	@Override
	protected WigleLocation getLocation(Cursor locationData) {
		return new WigleLocation(locationData);
	}

	@Override
	protected String getNetworkQuerySelection() {
		return String.format("(%s != '') AND (%s != 0)", WigleNetwork.Columns.SSID, WigleNetwork.Columns.LASTTIME);
	}
	
	@Override
	protected String getLocationQuerySelection() {
		return String.format("(%s != 0)", WigleLocation.Columns.TIME);
	}
	
	@Override
	protected String getNetworkOrderBy() {
		return String.format("%s DESC", WigleNetwork.Columns.LASTTIME);
	}
	
	@Override
	protected String getLocationOrderBy() {
		return String.format("%s DESC", WigleLocation.Columns.TIME);
	}
}
