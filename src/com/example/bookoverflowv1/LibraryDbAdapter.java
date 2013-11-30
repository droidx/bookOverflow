package com.example.bookoverflowv1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LibraryDbAdapter {
	
	private static final String TAG = "LibraryDbAdapter";
	
	public static final String BOOK_TITLE = "booktitle";
	public static final String ISSUE_DATE = "issuedate";
	public static final String DUE_DATE = "duedate";
	public static final String BOOK_ID = "bookid";
	public static final String KEY_ID = "_id";
	
	private static final String DATABASE_NAME = "students";
	private static final String DATABASE_TABLE = "library";
	private static final int DATABASE_VERSION = 2;
	
	private DatabaseHelper mDbHelper;
    public static SQLiteDatabase mDb;
	
	private static final String DATABASE_CREATE = 
			"create table library (_id integer primary key autoincrement,"
			+ "bookid text not null,booktitle text not null, issuedate text not null, duedate text not null);";
			
	private final Context mCtx;
	private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
		}
		
	}
	
	public LibraryDbAdapter(Context ctx){
		this.mCtx = ctx;
	}
	
	public LibraryDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    public long createEntry(String title, String issuedate, String duedate , String bookid) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(BOOK_ID,bookid);
        initialValues.put(BOOK_TITLE, title);
        initialValues.put(ISSUE_DATE, "Date Of Issue : "+issuedate);
        initialValues.put(DUE_DATE, "Due Date : "+duedate);       
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteAll() {
        return mDb.delete(DATABASE_TABLE, null, null) > 0;
    }

    public Cursor fetchAllEntries() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ID,BOOK_ID,BOOK_TITLE, ISSUE_DATE,
                DUE_DATE}, null, null, null, null, null);
    }

    /*public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_TITLE, KEY_BODY}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }*/

    /*public boolean updateNote(long rowId, String title, String body) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }*/
}
