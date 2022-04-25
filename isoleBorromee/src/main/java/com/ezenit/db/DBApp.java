package com.ezenit.db;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DBApp extends Application{
	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================
	private static SQLiteOpenHelper dbHelper;
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		dbHelper = getDBHelper();
		
	}
	
	public static SQLiteDatabase getDB(){
		return dbHelper.getWritableDatabase();
	}
	
	// ===========================================================
	// Methods
	// ===========================================================
	public abstract SQLiteOpenHelper getDBHelper();
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
