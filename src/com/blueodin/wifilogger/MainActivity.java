package com.blueodin.wifilogger;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import com.blueodin.wifilogger.data.NetworkEntry;
import com.blueodin.wifilogger.data.WirelessNetworkEntries;
import com.blueodin.wifilogger.fragments.GraphSectionFragment;
import com.blueodin.wifilogger.fragments.ImportDetailFragment;
import com.blueodin.wifilogger.fragments.MapSectionFragment;
import com.blueodin.wifilogger.fragments.OverviewSectionFragment;
import com.blueodin.wifilogger.fragments.SectionDetailFragment;
import com.blueodin.wifilogger.fragments.lists.ImportListFragment;
import com.blueodin.wifilogger.fragments.lists.MultiSelectNetworkListFragment;
import com.blueodin.wifilogger.fragments.lists.NetworkListFragment;
import com.blueodin.wifilogger.fragments.lists.NetworkListInterface;
import com.blueodin.wifilogger.upload.UploadTask;
import java.util.List;

public class MainActivity extends FragmentActivity implements
		NetworkListFragment.OnNetworkSelectedCallback,
		LoaderCallbacks<WirelessNetworkEntries>, TabListener {
	public static final String PARAM_FROM_SERVICE = "param_from_service";
	public static final String TAG_NETWORK_LIST = "tag_network_list";
	public static final String TAG_DETAIL_SECTION = "tag_detail_section";
	
	private static final String ARG_SELECTED_TAB = "arg_selected_tab";
	
	private static final int IDX_OVERVIEW = 0;
	private static final int IDX_GRAPH = 1;
	private static final int IDX_MAP = 2;
	private static final int IDX_IMPORT = 3;

	private MenuItem mToggleServiceMenuItem = null;

	private WirelessNetworkEntries mNetworkEntries = null;

	private SharedPreferences mSharedPreferences;

	private boolean mBound = false;
	
	private SparseArray<SectionDetailFragment> mSections = new SparseArray<SectionDetailFragment>();

	private ActionBar mActionBar;

	@Override
	protected void onStart() {
		super.onStart();

		bindService(new Intent(this, WifiLockService.class),
				mServiceConnection, 0);
		updateMenuItem();
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (mBound) {
			unregisterReceiver(mWifiScanReceiver);
			unbindService(mServiceConnection);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateMenuItem();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		mActionBar = getActionBar();

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mSections.put(IDX_OVERVIEW, new OverviewSectionFragment());
		mSections.put(IDX_GRAPH, new GraphSectionFragment());
		mSections.put(IDX_MAP, new MapSectionFragment());
		mSections.put(IDX_IMPORT, new ImportDetailFragment());

		for (int i = 0; i < mSections.size(); i++) {
			SectionDetailFragment f = mSections.get(i);
			mActionBar.addTab(
					mActionBar.newTab().setText(f.getTitle()).setTag(f)
							.setTabListener(this), i);
		}

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(ARG_SELECTED_TAB))
				mActionBar.setSelectedNavigationItem(savedInstanceState
						.getInt(ARG_SELECTED_TAB));
		}

		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_SELECTED_TAB, mActionBar.getSelectedTab()
				.getPosition());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		mToggleServiceMenuItem = menu.findItem(R.id.menu_toggle_logger);
		updateMenuItem();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_exit:
			if (mBound)
				toggleScanService();
			finish();
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_toggle_logger:
			toggleScanService();
			return true;
		case R.id.menu_upload_data:
			uploadData();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void uploadData() {
		UploadTask uploadTask = new UploadTask(this,
				SettingsActivity.getServiceUri(mSharedPreferences,
						getResources()));
		uploadTask.execute(mNetworkEntries);
	}

	private void updateMenuItem() {
		if (mToggleServiceMenuItem == null)
			return;

		mToggleServiceMenuItem.setIcon(mBound ? R.drawable.ic_action_stop
				: R.drawable.ic_action_start);
		mToggleServiceMenuItem.setChecked(mBound);
	}

	private void toggleScanService() {
		Intent serviceIntent = new Intent(this, WifiLockService.class);

		if (mBound) {
			unregisterReceiver(mWifiScanReceiver);
			stopService(serviceIntent);
			unbindService(mServiceConnection);
			mBound = false;
		} else {

			startService(serviceIntent);
		}

		updateMenuItem();
	}

	public WirelessNetworkEntries getNetworkEntries() {
		return mNetworkEntries;
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			//mService = ((WifiLockServiceBinder) service).getService();
			mBound = true;
			registerReceiver(mWifiScanReceiver, new IntentFilter(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			updateMenuItem();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
			//mService = null;
			unregisterReceiver(mWifiScanReceiver);
			updateMenuItem();
		}
	};

	private WifiStateReceiver mWifiScanReceiver = new WifiStateReceiver() {
		@Override
		public void updateResults(final List<ScanResult> results) {
			(new AsyncTask<WirelessNetworkEntries, Void, Void>() {
				protected Void doInBackground(WirelessNetworkEntries... params) {
					for (ScanResult result : results)
						params[0].add(result);

					return null;
				}

				protected void onPostExecute(Void result) {
					updateNetworkList();
					updateDetailFragment();
				}
			}).execute(mNetworkEntries);
		}
	};

	public SectionDetailFragment getSelectedTab() {
		return (SectionDetailFragment) mActionBar.getSelectedTab().getTag();
	}

	public void updateNetworkList() {

	}

	public void updateDetailFragment() {

	}

	@Override
	public void onNetworkSelected(NetworkEntry network) {
		SectionDetailFragment f = getSelectedTab();

		if (f == null)
			return;

		f.onNetworkSelected(network);
	}

	@Override
	public Loader<WirelessNetworkEntries> onCreateLoader(int id,
			Bundle arguments) {
		return new WirelessNetworkEntries.RecordsLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<WirelessNetworkEntries> loader,
			WirelessNetworkEntries results) {
		mNetworkEntries = results;

		NetworkListInterface listFragment = (NetworkListInterface) getSupportFragmentManager().findFragmentByTag(TAG_NETWORK_LIST);
		if (listFragment == null)
			return;

		listFragment.updateNetworks();
	}

	@Override
	public void onLoaderReset(Loader<WirelessNetworkEntries> loader) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		Fragment networkList;

		switch (tab.getPosition()) {
		case IDX_MAP:
			networkList = new MultiSelectNetworkListFragment();
			break;
		case IDX_IMPORT:
			networkList = new ImportListFragment();
			break;
		default:
			networkList = new NetworkListFragment();
		}

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frame_main_network_list, networkList,
						TAG_NETWORK_LIST)
				.replace(R.id.frame_main_content,
						mSections.get(tab.getPosition()), TAG_DETAIL_SECTION)
				.commit();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}
}