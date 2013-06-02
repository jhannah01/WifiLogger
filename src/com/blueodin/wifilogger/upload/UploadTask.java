package com.blueodin.wifilogger.upload;

import android.content.Context;
import android.os.AsyncTask;
import com.blueodin.wifilogger.data.LocationEntry;
import com.blueodin.wifilogger.data.NetworkEntry;
import com.blueodin.wifilogger.data.WirelessNetworkEntries;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UploadTask extends AsyncTask<WirelessNetworkEntries, Integer, UploadResponse> {
	private WirelessNetworkEntries mNetworkEntries;
	private String mServiceUri;
	private Context mContext;
	
	public UploadTask(Context context, String serviceUri) {
		super();
		mContext = context;
		mServiceUri = serviceUri;
	}
	
	@Override
	protected UploadResponse doInBackground(WirelessNetworkEntries... params) {
		UploadResponse response;
		HashMap<NetworkEntry, List<LocationEntry>> entries = new HashMap<NetworkEntry, List<LocationEntry>>();
	
		mNetworkEntries = params[0];
		
		for(NetworkEntry entry : mNetworkEntries.getNetworks()) {
			List<LocationEntry> locations = new ArrayList<LocationEntry>();
			for(LocationEntry location : mNetworkEntries.getLocations(entry)) {
				if(location.synced)
					continue;
				locations.add(location);
			}
			entries.put(entry, locations);
		}
		
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(mServiceUri);
			
			String jsonData = (new Gson()).toJson(entries);
			
			httpPost.setEntity(new StringEntity(jsonData));
						
			httpPost.setHeader("Content-type", "application/json");
			
			HttpResponse httpResponse = httpClient.execute(httpPost);
            InputStream inputStream = httpResponse.getEntity().getContent();
            
            JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));
            
            response = (new Gson()).fromJson(jsonReader, UploadResponse.class);
            
            jsonReader.close();
            inputStream.close();
            
            if(response.success) {
	            for(List<LocationEntry> locations : entries.values()) {
	            	for(LocationEntry entry : locations)
	            		entry.setSynced(mContext.getContentResolver());
	            }
            }
            
            return response;
		} catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            response = new UploadResponse(false, ex.getMessage());
        } catch (ClientProtocolException ex) {
            ex.printStackTrace();
            response = new UploadResponse(false, ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
            response = new UploadResponse(false, ex.getMessage());
        }
		
		return new UploadResponse(false, "Unknown Error");
	}
}
