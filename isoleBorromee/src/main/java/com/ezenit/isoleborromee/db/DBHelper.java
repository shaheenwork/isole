package com.ezenit.isoleborromee.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import com.ezenit.isoleborromee.db.table.TableAudioGuide;
import com.ezenit.isoleborromee.db.table.TableDownloadQueue;
import com.ezenit.isoleborromee.db.table.TableImgRel;
import com.ezenit.isoleborromee.db.table.TableMap;
import com.ezenit.isoleborromee.db.table.TablePhotoGallery;
import com.ezenit.isoleborromee.db.table.TableSection;
import com.ezenit.utils.MiscUtils;
import com.ezenit.isoleborromee.R;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	
	// ===========================================================
	// Constants
	// ===========================================================
	
	private static final String DB_NAME 	= "isole.db";
	private static final int	DB_VERSION  = 1;
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	private static DBHelper instance;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public static DBHelper getInstance(Context context){
		if(instance==null){
			instance = new DBHelper(context);
		}
		return instance;
	}
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void onCreate(SQLiteDatabase db) {
		TableAudioGuide.onCreate(db);
		TableImgRel.onCreate(db);
		TablePhotoGallery.onCreate(db);
		TableMap.onCreate(db);
		TableDownloadQueue.onCreate(db);
		TableSection.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	// ===========================================================
	// Methods
	// ===========================================================
	public static void exportDB(Context context)
	{
		FileChannel src = null;
		FileChannel dst = null;
		try 
			{
				File sd 	= Environment.getExternalStorageDirectory();
				File data 	= Environment.getDataDirectory();
        
        if (sd.canWrite()) 
        	{
            	String currentDBPath = "//data//"+context.getPackageName()+"//databases//"+DB_NAME;
            	String backupDBPath =	DB_NAME;
            	File currentDB = new File(data, currentDBPath);
            	File backupDB = new File(sd, backupDBPath);
            	if (currentDB.exists()) 
            		{
            			src = new FileInputStream(currentDB).getChannel();
            			dst = new FileOutputStream(backupDB).getChannel();
            			dst.transferFrom(src, 0, src.size());
            			src.close();
            			dst.close();
            		}
        	}
        } 
		catch (Exception e) 
		{
			MiscUtils.showToast(context, R.string.export_failed);
		}
	}

	public ArrayList<Cursor> getData(String Query){
		//get writable database
		SQLiteDatabase sqlDB = this.getWritableDatabase();
		String[] columns = new String[] { "message" };
		//an array list of cursor to save two cursors one has results from the query
		//other cursor stores error message if any errors are triggered
		ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
		MatrixCursor Cursor2= new MatrixCursor(columns);
		alc.add(null);
		alc.add(null);

		try{
			String maxQuery = Query ;
			//execute the query results will be save in Cursor c
			Cursor c = sqlDB.rawQuery(maxQuery, null);

			//add value to cursor2
			Cursor2.addRow(new Object[] { "Success" });

			alc.set(1,Cursor2);
			if (null != c && c.getCount() > 0) {

				alc.set(0,c);
				c.moveToFirst();

				return alc ;
			}
			return alc;
		} catch(SQLException sqlEx){
			Log.d("printing exception", sqlEx.getMessage());
			//if any exceptions are triggered save the error message to cursor an return the arraylist
			Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
			alc.set(1,Cursor2);
			return alc;
		} catch(Exception ex){
			Log.d("printing exception", ex.getMessage());

			//if any exceptions are triggered save the error message to cursor an return the arraylist
			Cursor2.addRow(new Object[] { ""+ex.getMessage() });
			alc.set(1,Cursor2);
			return alc;
		}
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
