package com.example.bookoverflowv1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
//import java.util.Calendar;
import java.util.Date;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;


public class CalSync extends Activity{
			
	String s1 = "content://calendar/calendars";
	String s2 = "content://com.android.calendar/calendars";
	
	Date convertedDate;
	Date date;
	
	long startMillis =0;
	long endMillis = 0;
	int year,month,day;
	String dateString = "";
	public static long timeInMillis[] = new long [50];
	public static String getIndex[]= new String[50];
	long m;
	int k=0;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync);
		//Log.d("obj.arr.length", MainActivity.arr.length+"   " + MainActivity.arr[2]);
		for(int i=2;i<(MainActivity.arr.length)&&(MainActivity.arr[i]!=null);i+=3,k++)
		{
			/*String a[]=MainActivity.arr[i].split("-");
			try{
			year = Integer.parseInt(a[0]);
			month = Integer.parseInt(a[1]);
			day = Integer.parseInt(a[2]);
			dateString = year+"/"+month+"/"+day;
			}
			catch (Exception e){}
			Log.d("year month day", year+"   "+month + "   " +day);
			Log.d("dateString is ", dateString);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/mm/dd");
			try {
				convertedDate = dateFormat.parse(dateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Date newDate = new Date();
			
		    Log.d("Converted string to date : " ,convertedDate+"");
		    Log.d("Milliseconds since January 1, 1970, 00:00:00 GMT : " ,convertedDate.getTime()+"");
		    getIndex[k]=addEvent(convertedDate.getTime(),MainActivity.arr[i-2]);
		    timeInMillis[k] = convertedDate.getTime();
		    */
			SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd");
			String str = MainActivity.arr[i];
			try {
				date = sdfSource.parse(str);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getIndex[k]=addEvent(date.getTime(),MainActivity.arr[i-2]);
		    timeInMillis[k] = date.getTime();
			
		}
		Log.d("k in Calsync is", k+"");
		if(MainActivity.count!=0)
		{
		for(int n = 0 ;n<(int)MainActivity.count;n++)
		{
		DeleteCalendarEntry(Integer.parseInt(MainActivity.id[n]));
		}
		}
		if(k!=0)
		{
		Intent intent = new Intent();
	    setResult(Activity.RESULT_OK, intent);
		}
		else
		{
		//Intent intent = new Intent();
		setResult(Activity.RESULT_CANCELED);
		}
	    Log.d("RESULT in Calsync", "finished");
	    finish();
		
		
	}

   // private String m_selectedCalendarId = "0";	
   
	/*public void addEvent(long startMillis,String bookName)
	{
		ContentValues l_event = new ContentValues();
    	l_event.put("calendar_id", "1");
    	l_event.put("title", "Library Book Return");
    	l_event.put("description", "Today is the due Date for "+bookName);
    	l_event.put("eventLocation", "@library");
    	startMillis+=10*60*60*1000;
    	l_event.put("dtstart", startMillis);
    	//l_event.put("startTime","17/1/2012" );
    	l_event.put("dtend", startMillis+ 60*7*60*1000);
    	l_event.put("allDay", 0);
    	//status: 0~ tentative; 1~ confirmed; 2~ canceled
    	l_event.put("eventStatus", 1);
    	//0~ default; 1~ confidential; 2~ private; 3~ public
    	l_event.put("visibility", 0);
    	//0~ opaque, no timing conflict is allowed; 1~ transparency, allow overlap of scheduling
    	l_event.put("transparency", 0);
    	//0~ false; 1~ true
    	l_event.put("hasAlarm", 1);
    	Uri l_eventUri;
    	if (Build.VERSION.SDK_INT >= 8) {
    		l_eventUri = Uri.parse("content://com.android.calendar/events");
    	} else {
    		l_eventUri = Uri.parse("content://calendar/events");
    	}
    	Log.d("adding event", "added");
    	Uri l_uri = this.getContentResolver().insert(l_eventUri, l_event);
    	if (Build.VERSION.SDK_INT >= 8) {
    		l_uri = Uri.parse(s2);
    	} else {
    		l_uri = Uri.parse(s1);
    	}
    	
    	l_event = new ContentValues();
    	Uri REMINDERS_URI;
    	if (Build.VERSION.SDK_INT >= 8) {
    		REMINDERS_URI = Uri.parse(s2);
    	} else {
    		REMINDERS_URI= Uri.parse(s1);
    	}
    	try{
    	l_event.put("event_id",Long.parseLong(l_eventUri.getLastPathSegment()));
    	}
    	catch(Exception e)
    	{}
    	
    	l_event.put("method", 1);
    	l_event.put("minutes", 10);
    	Uri remind_uri ;
    	if (Build.VERSION.SDK_INT >= 8) {
    		remind_uri = Uri.parse(s2);
    	} else {
    		remind_uri= Uri.parse(s1);
    	}
    	REMINDERS_URI = this.getContentResolver().insert(remind_uri, l_event);
    	
    	Log.d("event id : ", Long.parseLong(l_eventUri.getLastPathSegment())+"");
    	Log.v("++++++test", l_uri.toString());
    	Log.v("++++++test", REMINDERS_URI.toString());
	}*/
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
    private static String getCalendarUriBase(Activity act) {

        String calendarUriBase = null;
        Uri calendars = Uri.parse("content://calendar/calendars");
        Cursor managedCursor = null;
        try {
            managedCursor = act.managedQuery(calendars, null, null, null, null);
        } catch (Exception e) {
        }
        if (managedCursor != null) {
            calendarUriBase = "content://calendar/";
        } else {
            calendars = Uri.parse("content://com.android.calendar/calendars");
            try {
                managedCursor = act.managedQuery(calendars, null, null, null, null);
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
    public void DeleteCalendarEntry2(int entryID , Context c) {
        int iNumRowsDeleted = 0;
        Uri eventsUri = Uri.parse(CalSync.getCalendarUriBase(this)+"events");
        //Log.d("CalSyncthis", getCalendarUriBase(getParent()));
        Uri eventUri = ContentUris.withAppendedId(eventsUri, entryID);
        iNumRowsDeleted = c.getContentResolver().delete(eventUri, null,null);
        Log.i("tag", "Deleted " + iNumRowsDeleted + " calendar entry.");

    }



}
