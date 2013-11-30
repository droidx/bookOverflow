package com.example.bookoverflowv1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class ConnectivityActionReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
		    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		    if(networkInfo.isConnected()) {
		        // Wifi is connected
		        Log.d("Inetify", "Wifi is connected: " + String.valueOf(networkInfo));
		        context.startService(new Intent(context,UpdaterService.class));
		    }
		} else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
		    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		    if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && ! networkInfo.isConnected()) {
		        // Wifi is disconnected
		        Log.d("Inetify", "Wifi is disconnected: " + String.valueOf(networkInfo));
		        context.stopService(new Intent(context,UpdaterService.class));
		    }
		}
	}
}
