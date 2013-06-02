package com.blueodin.wifilogger.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blueodin.wifilogger.MainActivity;
import com.blueodin.wifilogger.R;
import com.blueodin.wifilogger.SignalGraph;
import com.blueodin.wifilogger.data.LocationEntry;
import com.blueodin.wifilogger.data.NetworkEntry;
import com.blueodin.wifilogger.data.WirelessNetworkEntries;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GraphSectionFragment extends SectionDetailFragment {
	private static final String ARG_NETWORK_BSSID = "network_bssid";
	private static final String ARG_LAST_TIMESTAMP = "last_timestamp";
	private NetworkEntry mNetwork = null;
	private GraphViewSeries mGraphViewSeries;
	private long mLastTimestamp = 0;
	private TextView mEmptyTextView;
	private RelativeLayout mGraphLayout;
	private TextView mSSIDTextView;
	private TextView mBSSIDTextView;
	private LineGraphView mGraphView;
	private SignalGraph mSignalGraph;
	private WirelessNetworkEntries mNetworkEntries;

	public GraphSectionFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String bssid = "";

		mNetworkEntries = ((MainActivity)getActivity()).getNetworkEntries();
		
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(ARG_NETWORK_BSSID))
				bssid = savedInstanceState.getString(ARG_NETWORK_BSSID);

			if (savedInstanceState.containsKey(ARG_LAST_TIMESTAMP))
				mLastTimestamp = savedInstanceState
						.getLong(ARG_LAST_TIMESTAMP);
		}

		if (!bssid.isEmpty())
			mNetwork = mNetworkEntries.getNetwork(bssid);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ARG_NETWORK_BSSID, (mNetwork == null) ? ""
				: mNetwork.bssid);
		outState.putLong(ARG_LAST_TIMESTAMP, mLastTimestamp);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_network_graph,
				container, false);

		mEmptyTextView = (TextView) view
				.findViewById(R.id.text_network_empty);
		mGraphLayout = (RelativeLayout) view
				.findViewById(R.id.frame_network_graph);
		mSSIDTextView = (TextView) view
				.findViewById(R.id.text_network_ssid);
		mBSSIDTextView = (TextView) view
				.findViewById(R.id.text_network_bssid);

		mSignalGraph = (SignalGraph) view.findViewById(R.id.signalGraph);

		mGraphView = new LineGraphView(getActivity(), "Group History") {
			@Override
			protected String formatLabel(double value, boolean isValueX) {
				if (!isValueX)
					return String.format("%d dBm", (int) value);

				return DateUtils.getRelativeTimeSpanString((long) value,
						System.currentTimeMillis(),
						DateUtils.SECOND_IN_MILLIS,
						DateUtils.FORMAT_ABBREV_RELATIVE).toString();
			}
		};

		mGraphViewSeries = new GraphViewSeries("Level",
				new GraphViewSeriesStyle(Color.GREEN, 2),
				new GraphViewData[] {});
		mGraphView.addSeries(mGraphViewSeries);

		mGraphView.setScalable(true);
		mGraphView.setScrollable(true);
		mGraphView.setDrawBackground(true);
		setupGraphView();

		((FrameLayout) view.findViewById(R.id.layout_network_graph))
				.addView(mGraphView);

		updateView();

		return view;
	}

	private void setupGraphView() {
		mGraphView.setViewPort(
				System.currentTimeMillis() - (5 * 60 * 1000),
				(5 * 60 * 1000));
	}

	@Override
	public void onSectionSelected(NetworkEntry networkEntry) {
		mNetwork = networkEntry;
		updateView();
	}

	private void updateView() {
		if (mNetwork == null) {
			mEmptyTextView.setVisibility(View.VISIBLE);
			mGraphLayout.setVisibility(View.GONE);
		} else {
			mEmptyTextView.setVisibility(View.GONE);
			mGraphLayout.setVisibility(View.VISIBLE);
			mSSIDTextView.setText(mNetwork.ssid);
			mBSSIDTextView.setText(mNetwork.bssid);
			resetValues();
			updateGraph(true);
		}
	}

	private void resetValues() {
		mGraphViewSeries.resetData(new GraphViewData[] {});
		mSignalGraph.resetValues();
	}

	public void updateGraph() {
		updateGraph(false);
	}

	public void updateGraph(final boolean invertSort) {
		List<LocationEntry> locations = mNetworkEntries
				.getLocations(mNetwork);

		if (locations.size() < 1)
			return;

		Collections.sort(locations, new Comparator<LocationEntry>() {
			@Override
			public int compare(LocationEntry lhs, LocationEntry rhs) {
				if (lhs.timestamp < rhs.timestamp)
					return (invertSort ? -1 : 1);
				if (lhs.timestamp > rhs.timestamp)
					return (invertSort ? 1 : -1);
				return 0;
			}
		});

		for (int i = 0; i < locations.size(); i++) {
			LocationEntry location = locations.get(i);
			if (location.timestamp < mLastTimestamp)
				continue;
			mGraphViewSeries.appendData(new GraphViewData(
					location.timestamp, location.level), true);
			mSignalGraph.addValue(location.level, true);
			mLastTimestamp = location.timestamp;
		}
	}

	@Override
	public void onNetworkSelected(NetworkEntry networkEntry) {
		mNetwork = networkEntry;
		mGraphViewSeries.resetData(new GraphViewData[] {});
		mLastTimestamp = 0;
		setupGraphView();
		updateView();
	}

	@Override
	public String getTitle() {
		return "Graphs";
	}
}