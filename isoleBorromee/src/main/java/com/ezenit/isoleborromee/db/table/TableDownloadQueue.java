package com.ezenit.isoleborromee.db.table;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ezenit.db.DBTable;
import com.ezenit.isoleborromee.AppIsole.DownloadType;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;

public class TableDownloadQueue extends DBTable{
	// ===========================================================
	// Constants
	// ===========================================================

		
	private static final String TABLE_NAME			 = "download_queue";
	
	private static final String COLUMN_ID 			 = "_id";
	private static final String COLUMN_MUSUEM 		 = "musuem";
	private static final String COLUMN_LANGUAGE 	 = "language";
	private static final String COLUMN_TYPE 		 = "type";
	private static final String COLUMN_STATUS		 = "status";
	
	private static final String CREATE_TABLE		 = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("
															+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
															+COLUMN_MUSUEM+" TEXT, "
															+COLUMN_LANGUAGE+" TEXT, "
															+COLUMN_TYPE+" INTEGER, "
															+COLUMN_STATUS+" INTEGER)";
	
	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public static final void onCreate(SQLiteDatabase db){
		db.execSQL(CREATE_TABLE);
	}
	
	public static final void onUpgrade(int oldversion, int newversion, SQLiteDatabase db){
		
	}
	
	public final void insertOrUpdate(DownloadQueue queue){
		String selection = COLUMN_LANGUAGE+"=? AND "+
						   COLUMN_MUSUEM+" = ? AND "+
						   COLUMN_TYPE+" =?";
		
		String[] selectionArg = new String[]{queue.language,
											 queue.musuem.getShortName(),
											 queue.type.toString()};
		
		SQLiteDatabase db = getDB();
		
		Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_ID},
							selection,selectionArg, 
							null, null, null);
		
		if(c.moveToFirst()){
			c.close();
//			db.update(TABLE_NAME, queue.getValues(), selection, selectionArg);
		}
		else{
			c.close();
			db.insert(TABLE_NAME, null, queue.getValues());
		}
	}
	
	public final void delete(DownloadQueue queue){
		String selection = COLUMN_LANGUAGE+"=? AND "+
				   COLUMN_MUSUEM+" = ? AND "+
				   COLUMN_TYPE+" =?";

		String[] selectionArg = new String[]{queue.language,
											 queue.musuem.getShortName(),
											 queue.type.toString()};
		getDB().delete(TABLE_NAME, selection, selectionArg);
	}
	
	public static final void deleteAll(){
		getDB().delete(TABLE_NAME, null, null);
	}
	
	public static final DownloadQueue getTopQueue(){
		String selection = COLUMN_STATUS+"!= ?";
		String[] selectionArg = new String[]{String.valueOf(DownloadQueue.STATUS_COMPLETE)};
		Cursor c = getDB().query(TABLE_NAME, new String[]{COLUMN_LANGUAGE
							,COLUMN_MUSUEM
							,COLUMN_STATUS
							,COLUMN_TYPE}
							,selection ,selectionArg , null, null
							, COLUMN_ID+" ASC", "1");
		DownloadQueue queue = null;
		if(c.moveToFirst()){
			queue = new DownloadQueue(c);
		}
		c.close();
		return queue;
	}
	
	public DownloadQueue getNextInQueue() {
		String selection = COLUMN_STATUS+"!= ?";
		String[] selectionArg = new String[]{String.valueOf(DownloadQueue.STATUS_COMPLETE)};
		Cursor c = getDB().query(TABLE_NAME, new String[]{COLUMN_LANGUAGE
							,COLUMN_MUSUEM
							,COLUMN_STATUS
							,COLUMN_TYPE}
							,selection ,selectionArg , null, null
							, COLUMN_ID+" ASC", "2");
		DownloadQueue queue = null;
		if(c.getCount()>1&&c.moveToFirst()){
			if(c.moveToNext()){
				queue = new DownloadQueue(c);
			}
			
		}
		c.close();
		return queue;
	}
	
	public final ArrayList<DownloadQueue> getAllDownloads(){
		ArrayList<DownloadQueue> queue = new ArrayList<DownloadQueue>();
		populateAllDownloads(queue);
		return queue;
	}
	
	public final void populateAllDownloads(ArrayList<DownloadQueue> queue){
		Cursor c = getDB().query(TABLE_NAME, new String[]{COLUMN_LANGUAGE
							,COLUMN_MUSUEM
							,COLUMN_STATUS
							,COLUMN_TYPE}
							,null ,null , null, null
							, COLUMN_ID+" ASC");
		if(c.moveToFirst()){
			queue.add(new DownloadQueue(c));
		}
		c.close();
	}
	
	public final int getStatus(DownloadQueue queue){
		String selection = COLUMN_LANGUAGE+"=? AND "+
				   COLUMN_MUSUEM+" = ? AND "+
				   COLUMN_TYPE+" =?";

		String[] selectionArg = new String[]{queue.language,
									 queue.musuem.getShortName(),
									 queue.type.toString()};

		Cursor c = getDB().query(TABLE_NAME, new String[]{COLUMN_STATUS},
					selection,selectionArg, 
					null, null, null);
		int status = DownloadQueue.STATUS_COMPLETE;
		if(c.moveToFirst()){
			status = c.getInt(c.getColumnIndex(COLUMN_STATUS));
		}
		c.close();
		return status;
	}
	
	
	public final void updateStatus(DownloadQueue queue){
		String selection = COLUMN_LANGUAGE+"=? AND "+
				   COLUMN_MUSUEM+" = ? AND "+
				   COLUMN_TYPE+" =?";

		String[] selectionArg = new String[]{queue.language,
									 queue.musuem.getShortName(),
									 queue.type.toString()};
		getDB().update(TABLE_NAME, queue.getValues(), selection, selectionArg);
		
	}
	
	
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	public static class DownloadQueue{
		
		// ===========================================================
		// Constants
		// ===========================================================
		
		public static final int STATUS_DOWNLOADING 		 = 0;
		public static final int STATUS_FAILED			 = 1;
		public static final int STATUS_PAUSE			 = 2;
		public static final int STATUS_INSTALLING		 = 3;
		public static final int STATUS_WAITING			 = 4;
		public static final int STATUS_COMPLETE			 = 5;
		
		private static final ContentValues values = new ContentValues();
		
		// ===========================================================
		// Fields
		// ===========================================================
		
		
		private Museum 			musuem;
		private DownloadType	type;
		private String			language;
		private int				status;
		
		// ===========================================================
		// Constructors
		// ===========================================================

		public DownloadQueue(Museum musuem, DownloadType type,String language) {
			this.musuem 	= musuem;
			this.type   	= type;
			this.language 	= language;
			this.status 	= STATUS_WAITING;
		}
		
		public DownloadQueue(Cursor c) {
			this.musuem 	= Museum.getMuseum(c.getString(c.getColumnIndex(COLUMN_MUSUEM)));
			this.type		= DownloadType.getType(c.getString(c.getColumnIndex(COLUMN_TYPE)));
			this.language	= c.getString(c.getColumnIndex(COLUMN_LANGUAGE));
			this.status		= c.getInt(c.getColumnIndex(COLUMN_STATUS));
		}
		
		
		// ===========================================================
		// Getter & Setter
		// ===========================================================
		public void setStatus(int status) {
			this.status = status;
		}
		
		public DownloadType getType() {
			return type;
		}
		
		public Museum getMusuem() {
			return musuem;
		}
		
		public String getLanguage() {
			return language;
		}
		
		public ContentValues getValues(){
			values.clear();
			
			values.put(COLUMN_LANGUAGE, language);
			values.put(COLUMN_TYPE, type.toString());
			values.put(COLUMN_MUSUEM, musuem.getShortName());
			values.put(COLUMN_STATUS, status);
			
			return values;
		}
		
		// ===========================================================
		// Methods for/from SuperClass/Interfaces
		// ===========================================================

		// ===========================================================
		// Methods
		// ===========================================================

		// ===========================================================
		// Inner and Anonymous Classes
		// ===========================================================
		
	}

	
}
