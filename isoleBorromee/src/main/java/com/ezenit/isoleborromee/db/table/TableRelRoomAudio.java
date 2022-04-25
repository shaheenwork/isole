package com.ezenit.isoleborromee.db.table;

import android.database.sqlite.SQLiteDatabase;

public class TableRelRoomAudio{

	
	// ===========================================================
	// Constants
	// ===========================================================
	
	protected static final String TABLE_NAME      = "tableRelRoomAudio";
	
	protected static final String COLUMN_ID 	  = "_id";
	protected static final String COLUMN_ID_AUDIO = "audioId";
	protected static final String COLUMN_ID_ROOM  = "roomId";
	
	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("
														+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
														+COLUMN_ID_AUDIO+" INTEGER, "
														+COLUMN_ID_ROOM+" INTEGER)";
	
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
	
	public static void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_TABLE);
	}

	
	public static void onUpgrade(int oldVersion, int newVersion, SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}
	// ===========================================================
	// Methods
	// ===========================================================
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
