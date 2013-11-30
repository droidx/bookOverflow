package com.example.bookoverflowv1;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Templates;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class MainActivity extends ListActivity {
	
	//private static final String TAG = "MainActivity";
	
	SharedPreferences preferences;
	private ProgressDialog progressDialog;
	TimelineReceiver receiver;
	IntentFilter filter;
	boolean flag = true;
	String err;
	Cursor c ;

	public static boolean keep_track =false;
	String isbn;
	
	public static String arr[] = new String [50];
	public static String id[] = new String [50];
	public static long count;
	
	int REQUEST_CODE = 3;
	
	private LibraryDbAdapter mDbHelper;
	CalSync obj = new CalSync();
	
	boolean connected = false;
	String s1 = "192.168.1.205",s2 = "14.139.98.28";
	
	private ProgressDialog pd;
	int numberOfbooks;
	
	byte[] data;
	
	
	HttpPost httppost;
	StringBuffer buffer;
	HttpClient httpclient;
	HttpResponse response;
	InputStream input;
	
	List<NameValuePair> nameValuePairs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.librarylist);
		
		mDbHelper = new LibraryDbAdapter(this);
		mDbHelper.open();
		UpdaterService temp = new UpdaterService();
		CalSync obj = new CalSync();
		SQLiteStatement S = LibraryDbAdapter.mDb.compileStatement("select count(*) from library");
		long count = S.simpleQueryForLong();
		Log.d("count is ", count+"");
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		Editor edit = preferences.edit();
		String Rollno = preferences.getString("RollNo",null);
		String Server = preferences.getString("Server", "192.168.1.205");
		String address = preferences.getString("ServerAddress", "one");
		boolean autosync = preferences.getBoolean("auto_sync", true);
		if(autosync)
		{
			if(!UpdaterService.runFlag)
	        	startService(new Intent(this,UpdaterService.class));
		}
		else
			stopService(new Intent(this,UpdaterService.class));
		Log.d("address", address);
		if(address.equals("Outside Campus"))
			Server = s2;
		else
			Server = s1;
		boolean firstTime = preferences.getBoolean("FirstTime", true);
		numberOfbooks = preferences.getInt("numberOfbooks",0);
		Log.d("on create number of books", numberOfbooks+"");
		edit.putString("RollNo", Rollno);
		edit.putString("Server", Server);
		edit.commit();
		Log.d("selected server address", preferences.getString("Server", s1));
		receiver = new TimelineReceiver();
		filter = new IntentFilter("com.example.LibrarySamplev2.NEW_UPDATE");
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || 
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;
        Log.d("first time", firstTime+"");
        if(firstTime)
        {
        	AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
			alertDialog.setTitle("Using the app");
			alertDialog.setMessage("First, Make sure you enter your RollNo, Location and AutoSync using the \"User Settings\" from the menu\nOnce you are done, use the sync option from the menu");
			 alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  dialog.dismiss();				 
			    } });
			alertDialog.show();
        	Editor edit1 = preferences.edit();
        	edit1.putBoolean("FirstTime", false);
        	edit1.commit();
        	mDbHelper.deleteAll();
        }
        else
        {
        fillData();
        }
        progressDialog = new ProgressDialog(this);
	    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}
	
	

	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mDbHelper.close();
		//preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if(preferences.getBoolean("auto_sync", true))
		{
			if(!UpdaterService.runFlag)
	        	startService(new Intent(this,UpdaterService.class));
		}
		else
			stopService(new Intent(this,UpdaterService.class));
		
	}
	public void onAttachedToWindow() {
	    super.onAttachedToWindow();
	    try {
	        ((Activity) this).openOptionsMenu();        
	    } catch (Exception ex) {
	        Log.e("ERR", "Error: " + ex.getMessage());
	    }
	}
	public static void CancelNotification(Context ctx, int notifyId) {
	    String ns = Context.NOTIFICATION_SERVICE;
	    NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
	    nMgr.cancel(notifyId);
	}
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d("onResume"," Resumed");
		super.onResume();
		mDbHelper.open();
		Editor edit = preferences.edit();
		Log.d("auto sync on REsume", preferences.getBoolean("auto_sync", true)+"");
		if(preferences.getBoolean("auto_sync", true))
		{
			edit.putBoolean("auto_sync", true);
			if(!UpdaterService.runFlag)
			{
	        	startService(new Intent(this,UpdaterService.class));
			}
		}
		else
		{
			edit.putBoolean("auto_sync", false);
			stopService(new Intent(this,UpdaterService.class));
		}
		//preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String address = preferences.getString("ServerAddress", "0");
		String Server;
		String Rollno = preferences.getString("RollNo",null);
		if(Rollno!=null)
			{if(Rollno.trim().length()>1)
		{
			CancelNotification(this, 1);
		}
		}
		edit.putString("RollNo", Rollno);
		address = address.trim();
		Log.d("Server location", address);
		if(Integer.valueOf(address)==1)
		{
			Log.d("into if", "checking");
			Server = s2;
			edit.putString("ServerAddress", "1");
		}
		else
		{
			edit.putString("ServerAddress", "0");
			Server = s1;
		}
		edit.putString("Server", Server);
		Log.d("SErver address", Server);
		edit.commit();
		fillData();
		if(!keep_track)
		{
			validate();
			keep_track = true;
		}
	}
	

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mDbHelper.close();
		//preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if(preferences.getBoolean("auto_sync", true))
		{
			if(!UpdaterService.runFlag)
	        	startService(new Intent(this,UpdaterService.class));
		}
		else
			stopService(new Intent(this,UpdaterService.class));
	}

	public void syncissue (){
		Log.d("sync", "Into Sync");
    	mDbHelper.open();
    	c = mDbHelper.fetchAllEntries();
		startManagingCursor(c);
		id = new String [50];
		int h=0;
		while(c.moveToNext()) {
		    id[h] = c.getString(c.getColumnIndex("bookid"));
		    h++;
		    Log.d("id's are", Integer.parseInt(id[h-1])+"");
		}
    	SQLiteStatement S = LibraryDbAdapter.mDb.compileStatement("select count(*) from library");
		count = S.simpleQueryForLong();
		Log.d("count in menu is", count+"");
		for(int g=0;g<(int)count;g++)
		{
			int b = Integer.parseInt(id[g]);
			Log.d("after parsing", b+"");
		}
		mDbHelper.close();
		c.close();	
		runDialog();
	}
	
	
	
	
	public void validate()
	{
		String Rollno = preferences.getString("RollNo",null);
		//String address = preferences.getString("ServerAddress", "one");
		if(Rollno!=null)
		{
		if(Rollno.trim().length()>1)
		{
			
		String Server = preferences.getString("Server", null);
		/*if(address.equals("Outside Campus"))
			Server = s2;
		else
			Server = s1;*/
		//Editor edit = preferences.edit();
		//edit.commit();
		//edit.putString("Server", Server);
		httpclient= new DefaultHttpClient();
		httppost = new HttpPost("http://"+Server.trim()+"/validation.php");
		nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("user", Rollno.trim()));
		new Validation().execute(nameValuePairs);
		
		}
		}
		else
			Toast.makeText(getApplicationContext(), "Please check the RollNo in the settings", Toast.LENGTH_LONG).show();
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
        case R.id.menusettings:
        	Intent newIntent = new Intent (this,PrefsActivity.class);
    		startActivity(newIntent);
    		return true;
        case R.id.sync:
        	validate();
		return true;
        case R.id.about:
        	AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
			alertDialog.setTitle("About :");
			alertDialog.setMessage("Sync your library issues with your calendar.\nUse Auto sync in settings to sync automatically.\n\nDeveloped by Pritesh Sankhe");
			 alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  dialog.dismiss();				 
			    } });
			alertDialog.show();
			return true;
		}

		return false;
	}
	/**/
	
	private void runDialog()
	{
		String Rollno = preferences.getString("RollNo",null);
		String Server = preferences.getString("Server", null);
		Log.d("Server address", Server);
		Log.d("ROll NO in run dialog and length", Rollno + Rollno.trim().length());
		if(Rollno.length()==0)
		{
			AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
			alertDialog.setTitle("Invalid Settings");
			alertDialog.setMessage("PLease check the Settings");
			 alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  dialog.dismiss();				 
			    } });
		
		}
		else {			
					String roll = Rollno.toString();
					Rollno = preferences.getString("RollNo",null);
			    	roll = Rollno.toString();
						
					httpclient= new DefaultHttpClient();
					httppost = new HttpPost("http://"+Server.trim()+"/testodbc.php");
					nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair("user", roll.trim()));
					new BookIssue().execute(nameValuePairs);
		}
	    	
	    	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==3)
		{
			if(resultCode == Activity.RESULT_OK){
			Log.d("Into Activity Result", "REsult_ok");
			mDbHelper.open();
			mDbHelper.deleteAll();
			createEntries();
			fillData();
			}
			if(resultCode == Activity.RESULT_CANCELED){
			mDbHelper.open();
			mDbHelper.deleteAll();
			fillData();
			}
		}
	}
	
	
	String allTitles;
	String curentDateandTime;
	public void fillData()
	{
		Cursor c = mDbHelper.fetchAllEntries();
		startManagingCursor(c);
		

		// loop through cursor 
		while(c.moveToNext()) {
		    allTitles= c.getString(c.getColumnIndex("duedate"));
		    Log.d("allTitles", allTitles+"");
		}

		String [] from = new String [] {LibraryDbAdapter.BOOK_TITLE,LibraryDbAdapter.ISSUE_DATE,LibraryDbAdapter.DUE_DATE};
		int to [] = new int [] {R.id.text1,R.id.text2,R.id.text3};
		SimpleCursorAdapter notes = new SimpleCursorAdapter(this, R.layout.libraryrow, c, from, to);
		setListAdapter(notes);
	}
	
	private void createEntries()
	{
		numberOfbooks = 0;
		Log.d("arr length in create entries",arr.length+"");
		for(int k=0,k1=0;k<arr.length;k+=3,k1++)
		{
			numberOfbooks+=1;
			
			String s = CalSync.getIndex[k1];
			mDbHelper.createEntry(arr[k], arr[k+1].substring(0, 10), arr[k+2].substring(0, 10),s);
			if((CalSync.timeInMillis[k1]+17*60*60*1000)<System.currentTimeMillis())
			{
				Log.d("System time in milliseconds", System.currentTimeMillis()+"");
				Toast.makeText(getApplicationContext(), "Due Date has already passed for "+ arr[k], Toast.LENGTH_LONG).show();
			}
		}
		Editor edit2 = preferences.edit();
		edit2.putInt("numberOfbooks",numberOfbooks);
		edit2.commit();
	}
	public class BookIssue extends AsyncTask<java.util.List<NameValuePair>, Void, Void>{

		
		
		private volatile boolean running = true;
		
		public BookIssue (){
			 progressDialog.setCancelable(true);
		        progressDialog.setOnCancelListener(new OnCancelListener() {
		            @Override
		            public void onCancel(DialogInterface dialog) {
		                // actually could set running = false; right here, but I'll
		                // stick to contract.
		                cancel(true);
		            }
		        });
		}
		protected void onCancelled() {
	        running = false;
	    }
		protected Void doInBackground(java.util.List<NameValuePair>... params) {
			// TODO Auto-generated method stub
			try{
				httppost.setEntity(new UrlEncodedFormEntity(params[0])); 
	            response = httpclient.execute(httppost);
	            input = response.getEntity().getContent();
				
				data = new byte[256];
	          	arr = new String [50];
	            buffer = new StringBuffer();
	               
	                    int len = 0;
	                    while (-1 != (len = input.read(data)) )
	                    {
	                        buffer.append(new String(data, 0, len));
	                        
	                    }
	                    String bf = buffer.toString();
	                    arr = bf.split("\n");
	                    input.close();       
	                    
	                    String str = buffer.toString();
	                    arr= str.split("\t");
	                    for(int j=0;j<arr.length;j++)
	                    {
	                    	Log.d("array", arr[j]);
	                    }
	                    
				Log.d("main", "processed");
				Intent calIntent = new Intent(MainActivity.this,CalSync.class);
				startActivityForResult(calIntent, REQUEST_CODE);
				flag=true;
				running = false;
			}
			catch (Exception e)
	        {
				Log.d("catch", e.toString());
				flag=false;
				err = e.toString();
				running = false;
	        }
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			progressDialog.dismiss();
			if(!flag)
			{
				AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
				alertDialog.setTitle("Server connection refused");
				alertDialog.setMessage("Check you wifi/internet settings or the server address in your settings");
				 alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int which) {
				    	  dialog.dismiss();				 
				    } });
				alertDialog.show();
				flag = true;
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Sync complete", Toast.LENGTH_LONG).show();
			}
			}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressDialog.show();
			progressDialog.setMessage("Fetching data...");
		}
		
	}
	
	
	public class Validation extends AsyncTask<java.util.List<NameValuePair>, Void, Void>{

		private volatile boolean running = true;
		
		public Validation (){
			 progressDialog.setCancelable(true);
		        progressDialog.setOnCancelListener(new OnCancelListener() {
		            @Override
		            public void onCancel(DialogInterface dialog) {
		                // actually could set running = false; right here, but I'll
		                // stick to contract.
		                cancel(true);
		            }
		        });
		}
		protected void onCancelled() {
	        running = false;
	    }

		protected Void doInBackground(java.util.List<NameValuePair>... params) {
			while(running)
			{
			try{
				httppost.setEntity(new UrlEncodedFormEntity(params[0])); 
	            response = httpclient.execute(httppost);
	            input = response.getEntity().getContent();
				
				data = new byte[256];
	          	arr = new String [50];
	            buffer = new StringBuffer();
	               
	                    int len = 0;
	                    while (-1 != (len = input.read(data)) )
	                    {
	                        buffer.append(new String(data, 0, len));
	                        
	                    }
	                    String bf = buffer.toString();
	                    arr = bf.split("\n");
	                    input.close();       
	                    for(int j=0;j<arr.length;j++)
	                    {
	                    	Log.d("array", arr[j]);
	                    }
	            running = false;
				Log.d("main", "processed");				
			
			}
			catch (Exception e)
	        {
				Log.d("catch", e.toString());
				flag=false;
				err = e.toString();
				running = false;
	           // Toast.makeText(getApplicationContext(), "error"+e.toString(), Toast.LENGTH_LONG).show();
	        }
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			if(!flag)
			{
				AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
				alertDialog.setTitle("Server connection refused");
				alertDialog.setMessage("Check you wifi/internet settings or the server address in your settings");
				 alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int which) {
				    	  dialog.dismiss();				 
				    } });
				alertDialog.show();
				flag = true;
			flag = true;
			}
			else if(arr[0].equals("n"))
			{
				Toast.makeText(getApplicationContext(), "the entered roll number is invalid", Toast.LENGTH_LONG).show();
			}
			else
			{
	        	syncissue();
			}
			}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressDialog.show();
			progressDialog.setMessage("validating rollno with the database...");
		}		
	}
	class TimelineReceiver extends BroadcastReceiver { // 
		  @Override
		  public void onReceive(Context context, Intent intent) { // 
		    fillData(); 
		    ((BaseAdapter) getListAdapter()).notifyDataSetChanged(); // 
		    Log.d("TimelineReceiver", "onReceived");
		  }
		}
	
}


