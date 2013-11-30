package com.example.bookoverflowv1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver{

	SharedPreferences preferences;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		boolean isNetworkDown = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		if(isNetworkDown)
		{
			Log.d("NetworkReceiver", "onReceive : NOT connected,stopping UpdaterService");
			context.stopService(new Intent(context,UpdaterService.class));
		}
		else{
			preferences = PreferenceManager.getDefaultSharedPreferences(context);
			Log.d("auto_sync", preferences.getBoolean("auto_sync", true)+"");
			if(preferences.getBoolean("auto_sync", true))
			{
				Log.d("NetworkReceiver", "onReceive : connected,stopping UpdaterService");
				context.startService(new Intent(context,UpdaterService.class));
			}
		}
	}

}
