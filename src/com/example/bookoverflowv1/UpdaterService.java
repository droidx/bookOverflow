package com.example.bookoverflowv1;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class UpdaterService extends Service{

	private static final String TAG = "UpdaterService";
	static final int DELAY = 1000*60*60*12; // a minute 
	public static boolean runFlag = false;  // 
	private Updater updater;
	public static String id[] = new String [50];
	MainActivity obj1 = new MainActivity();
	Cursor c ;
	Date date;
	HttpPost httppost;
	StringBuffer buffer;
	HttpClient httpclient;
	HttpResponse response;
	InputStream input;
	long m;
	byte[] data;
		
	int k=0,numberOfbooks=0;
	public static String arr[] = new String [50];
	boolean flag = true;
	String err;
	private LibraryDbAdapter mDbHelper;
	List<NameValuePair> nameValuePairs;
	SharedPreferences preferences;
	CalSync obj = new CalSync();
	  
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		this.updater = new Updater();
		data = new byte[256];
		arr = new String [50];
		mDbHelper = new LibraryDbAdapter(this);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);//
		Log.d(TAG, "onCreated");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.runFlag = false; // 
	    this.updater.interrupt(); // 
	    this.updater = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//return super.onStartCommand(intent, flags, startId);
		
		if(!runFlag && preferences.getBoolean("auto_sync", true)){
		this.runFlag = true; // 
	    this.updater.start();
		}
		else
		{
			runFlag = false;
			Log.d("in else", "hello");
			//this.updater.stop();
			this.stopSelf();
		}
	    
	    Log.d(TAG, "onStarted");
		return START_STICKY;
	}

	public void fetchData()
	{

		String Rollno = preferences.getString("RollNo",null);
		String Server = preferences.getString("Server", null);
		if(Rollno!=null)
		{
		if(Rollno.trim().length()>1)
		{
		httpclient= new DefaultHttpClient();
		httppost = new HttpPost("http://"+Server.trim()+"/testodbc.php");
		nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("user", Rollno.trim()));
		
		try{
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs)); 
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
			flag=true;
			
			CalendarActivities();
			
		}
		
	
		catch (Exception e)
        {
			Log.d("catch", e.toString());
			flag=false;
			err = e.toString();
        }
		}
		}
		else
		{
			String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		
		int icon = R.drawable.library;
		CharSequence tickerText = "Library sync error";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		Context context = getApplicationContext();
		CharSequence contentTitle = "Invalid Settings/roll No";
		CharSequence contentText = "Check your settings";
		
		/*Intent intent = new Intent(Intent.ACTION_VIEW);
		//Android 2.2+
		intent.setData(Uri.parse("content://com.android.calendar/events/" + String.valueOf(CalSync.getIndex[k1])));  */
		//Android 2.1 and below.
		//intent.setData(Uri.parse("content://calendar/events/" + String.valueOf(calendarEventID)));    
		/*intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		        | Intent.FLAG_ACTIVITY_SINGLE_TOP
		        | Intent.FLAG_ACTIVITY_CLEAR_TOP
		        | Intent.FLAG_ACTIVITY_NO_HISTORY
		        | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);*/
		//Uri eventDetailsUri = ContentUris.withAppendedId(Uri.parse(getCalendarUriBase(this)+"events"), Long.parseLong(CalSync.getIndex[k1]));
		//Intent intent = new Intent(Intent.ACTION_VIEW).setData(eventDetailsUri);
		//Log.d("start tym",eventDetailsUri.getQueryParameter("dtstart"));
		Intent intent = new Intent(UpdaterService.this,MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		final int HELLO_ID = 1;

		notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(HELLO_ID, notification);
		}
			
	}
	public void CalendarActivities()
	{
		mDbHelper.open();
    	c = mDbHelper.fetchAllEntries();
		id = new String [50];
		int h=0;
		while(c.moveToNext()) {
		    id[h] = c.getString(c.getColumnIndex("bookid"));
		    h++;
		    Log.d("id's are", Integer.parseInt(id[h-1])+"");
		}
		SQLiteStatement S = LibraryDbAdapter.mDb.compileStatement("select count(*) from library");
		MainActivity.count = S.simpleQueryForLong();
		
		for(int n = 0 ;n<(int)MainActivity.count;n++)
		{
		Log.d("into for", n +"   "+id[n] +  "Main id"+MainActivity.id[n]);
		try {
			DeleteCalendarEntry(Integer.parseInt(id[n]));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		Log.d("count", MainActivity.count+"");
		for(int i=2;i<arr.length&&arr[i]!=null;i+=3,k++)
		{
			SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd");
			String str = arr[i];
			try {
				date = sdfSource.parse(str);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CalSync.getIndex[k]=addEvent(date.getTime(),arr[i-2]);
		    CalSync.timeInMillis[k] = date.getTime();
		}
		createDatabaseEntries();
		//obj1.fillData();
		mDbHelper.close();
	}

	private void createDatabaseEntries()
	{
		mDbHelper.open();
		numberOfbooks = 0;
		mDbHelper.deleteAll();
		Log.d("arr length in create entries",arr.length+"");
		for(int k=0,k1=0;k<arr.length&&arr.length>1;k+=3,k1++)
		{
			numberOfbooks+=1;
			
			String s = CalSync.getIndex[k1];
			mDbHelper.createEntry(arr[k], arr[k+1].substring(0, 10), arr[k+2].substring(0, 10),s);
			Log.d("System milliseconds", System.currentTimeMillis()+"");
			long h = CalSync.timeInMillis[k1]+17*60*60*1000;
			Log.d("Calsync", h+"");
			
			if((CalSync.timeInMillis[k1]+17*60*60*1000)<System.currentTimeMillis())
			{
				Log.d("info if","LOL");
				String ns = Context.NOTIFICATION_SERVICE;
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
				
				int icon = R.drawable.library;
				CharSequence tickerText = "Library reminder";
				long when = System.currentTimeMillis();

				Notification notification = new Notification(icon, tickerText, when);
				Context context = getApplicationContext();
				CharSequence contentTitle = "Due date passed for";
				CharSequence contentText = arr[k];
				
				Log.d("System time in milliseconds", System.currentTimeMillis()+"" + "id is "+CalSync.getIndex[k1]);
				/*Intent intent = new Intent(Intent.ACTION_VIEW);
				//Android 2.2+
				intent.setData(Uri.parse("content://com.android.calendar/events/" + String.valueOf(CalSync.getIndex[k1])));  */
				//Android 2.1 and below.
				//intent.setData(Uri.parse("content://calendar/events/" + String.valueOf(calendarEventID)));    
				/*intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				        | Intent.FLAG_ACTIVITY_SINGLE_TOP
				        | Intent.FLAG_ACTIVITY_CLEAR_TOP
				        | Intent.FLAG_ACTIVITY_NO_HISTORY
				        | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);*/
				//Uri eventDetailsUri = ContentUris.withAppendedId(Uri.parse(getCalendarUriBase(this)+"events"), Long.parseLong(CalSync.getIndex[k1]));
				//Intent intent = new Intent(Intent.ACTION_VIEW).setData(eventDetailsUri);
				//Log.d("start tym",eventDetailsUri.getQueryParameter("dtstart"));
				Intent intent = new Intent(UpdaterService.this,MainActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

				notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
				final int HELLO_ID = 1;

				notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(HELLO_ID, notification);
				
				
			}
		}
		Editor edit2 = preferences.edit();
		edit2.putInt("numberOfbooks",numberOfbooks);
		edit2.commit();
		mDbHelper.close();
		c.close();
	}
	
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	public String addEvent(long startMillis, String bookName)
    {
    	//Calendar cal = Calendar.getInstance();     
        Uri EVENTS_URI = Uri.parse(getCalendarUriBase(this) + "events");
        ContentResolver cr = getContentResolver();

        // event insert
        ContentValues values = new ContentValues();
        values.put("calendar_id", 1);
        values.put("title", "Library Book Return");
        values.put("allDay", 0);
        startMillis+=9*60*60*1000;
        values.put("dtstart", startMillis); // event starts at 11 minutes from now
        values.put("dtend", startMillis+ 60*8*60*1000); // ends 60 minutes from now
        values.put("description", "Today is the due Date for "+bookName);
    	values.put("eventLocation", "@library");
        values.put("visibility", 0);
        values.put("hasAlarm", 1);
        Uri event = cr.insert(EVENTS_URI, values);

        // reminder insert
        Uri REMINDERS_URI = Uri.parse(getCalendarUriBase(this) + "reminders");
        values = new ContentValues();
        m=Long.parseLong(event.getLastPathSegment());
        Log.d("m is", m+"");
        values.put( "event_id", Long.parseLong(event.getLastPathSegment()));
        Log.d("event id "+ bookName,Long.parseLong(event.getLastPathSegment())+"");
        values.put( "method", 1 );
        values.put( "minutes", 10 );
        cr.insert( REMINDERS_URI, values );
        return m+"";
    }
	private static String getCalendarUriBase(Context context) {

        String calendarUriBase = null;
        Uri calendars = Uri.parse("content://calendar/calendars");
        Cursor managedCursor = null;
        try {
            managedCursor = context.getContentResolver().query(calendars, null, null, null, null);
        } catch (Exception e) {
        }
        if (managedCursor != null) {
            calendarUriBase = "content://calendar/";
        } else {
            calendars = Uri.parse("content://com.android.calendar/calendars");
            try {
                managedCursor = context.getContentResolver().query(calendars, null, null, null, null);
            } catch (Exception e) {
            }
            if (managedCursor != null) {
                calendarUriBase = "content://com.android.calendar/";
            }
        }
        //System.out.println(calendarUriBase);
        return calendarUriBase;
    }
	public void DeleteCalendarEntry(int entryID) {
        int iNumRowsDeleted = 0;
        Uri eventsUri = Uri.parse(getCalendarUriBase(this)+"events");
        Uri eventUri = ContentUris.withAppendedId(eventsUri, entryID);
        iNumRowsDeleted = getContentResolver().delete(eventUri, null,null);

        Log.i("tag", "Deleted " + iNumRowsDeleted + " calendar entry.");

    }
	

	
	private class Updater extends Thread {  // 

		
		Intent intent;
	    public Updater() {
	      super("UpdaterService-Updater");  // 
	    }

	    @Override
	    public void run() { // 
	      UpdaterService updaterService = UpdaterService.this;  // 
	      while (updaterService.runFlag) {  // 
	        Log.d(TAG, "Updater running");
	        try {
	          // Some work goes here...
	          //obj.validate();
	        	
			fetchData();	
	          Log.d(TAG, "Updater ran");
	          Thread.sleep(DELAY);  // 
	        } catch (InterruptedException e) {  // 
	          updaterService.runFlag = false;
	        }
	      }
	    }
	  } // Updater
	
}
