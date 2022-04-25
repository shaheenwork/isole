package com.ezenit.isoleborromee.db.table;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.ezenit.isoleborromee.db.DBUtils;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.AudioGuide;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;

public class TableImgRel {
	// ===========================================================
	// Constants
	// ===========================================================
	
	
	private static final String TABLE_NAME = "imgRelation";
		
	private static final String COLUMN_ID  	    = "id";
	private static final String COLUMN_REL_ID   = "rel_id";
	private static final String COLUMN_IMG_NAME = "img_name";
	private static final String COLUMN_IS_FREE  = "is_free";
	private static final String COLUMN_MUSEUM   = "museum";
	
	private static final String CREATE_TABLE 	= "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("
														+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
														+COLUMN_REL_ID+" TEXT, "
														+COLUMN_IMG_NAME+" TEXT, "
														+COLUMN_IS_FREE+" INTEGER, "
														+COLUMN_MUSEUM+" TEXT)";
	
	// ===========================================================
	// Fields
	// ===========================================================
	private static String[] columns;
	private static String[] selectionArg ;	
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	public static void onCreate(SQLiteDatabase db){
		db.execSQL(CREATE_TABLE);
	}
	
	public static void onUpgrade(SQLiteDatabase db){
		
	}
	
	// ===========================================================
	// Methods
	// ===========================================================
		
	public static void createOrUpdateImageRelation(SQLiteDatabase db,Image images){
		initSelection(1, 3);
		columns[0]		 = COLUMN_REL_ID;
		selectionArg[0]  = images.getRelId();
		selectionArg[1]  = images.getName();
		selectionArg[2]  = images.getMuseum().getShortName();
		if(!TextUtils.isEmpty(selectionArg[0])&&!TextUtils.isEmpty(selectionArg[1])){
			String selection = COLUMN_REL_ID+"=? AND "+COLUMN_IMG_NAME+" =? AND "+COLUMN_MUSEUM+" =?";
			Cursor c = db.query(TABLE_NAME,columns, selection, selectionArg, null, null, null);
			if(c.moveToFirst()){
				db.update(TABLE_NAME, images.getContentValues(),selection , selectionArg);
			}
			else{
				db.insert(TABLE_NAME, null, images.getContentValues());
			}
			c.close();
		}
	}
	
	public static ArrayList<Image> getAllImages(SQLiteDatabase db,AudioGuide audioGuide){
		ArrayList<Image> listImages = new ArrayList<Image>();
		getAllImages(db, audioGuide.getCodeNo(),audioGuide.getMuseum(),audioGuide.isFree(),listImages);
		return listImages;
	}
	
	public static ArrayList<Image> getAllImages(SQLiteDatabase db,String relId,Museum musuem, boolean isFree){
		ArrayList<Image> listImages = new ArrayList<Image>();
		getAllImages(db, relId,musuem,isFree,listImages);
		return listImages;
	}
	
	public static Image getSingleImage(SQLiteDatabase db,AudioGuide audioGuide){
		return getSingleImage(db, audioGuide.getCodeNo(), audioGuide.getMuseum(), audioGuide.isFree());
	}
	
	public static int getImageCount(SQLiteDatabase db,AudioGuide audioGuide){
		return getImageCount(db, audioGuide.getCodeNo(), audioGuide.getMuseum(), audioGuide.isFree());
	}
	
	public static Image getSingleImage(SQLiteDatabase db,String relId, Museum museum,boolean isFree){
		
		Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_REL_ID,COLUMN_IMG_NAME,COLUMN_IS_FREE,COLUMN_MUSEUM}
										, COLUMN_REL_ID+"=? AND "+COLUMN_MUSEUM+"=? AND "+COLUMN_IS_FREE+" =? ORDER BY "+COLUMN_IMG_NAME,
										new String[]{relId,museum.getShortName(),String.valueOf(DBUtils.boolToInt(isFree))} 
									, null, null, null);
		Image image = null;
		if(c.moveToFirst()){
				image = new Image(c);
				c.moveToNext();
		}
		c.close();
		return image;
	}
	
	public static int getImageCount(SQLiteDatabase db,String relId, Museum museum,boolean isFree){
		Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_REL_ID,COLUMN_IMG_NAME,COLUMN_IS_FREE,COLUMN_MUSEUM}
										, COLUMN_REL_ID+"=? AND "+COLUMN_MUSEUM+"=? AND "+COLUMN_IS_FREE+" =? ORDER BY "+COLUMN_IMG_NAME,
										new String[]{relId,museum.getShortName(),String.valueOf(DBUtils.boolToInt(isFree))} 
									, null, null, null);
		int number = c.getCount();
		c.close();
		return number;
	}
	
	public static void getAllImages(SQLiteDatabase db,String relId, Museum museum,boolean isFree,ArrayList<Image> listImages){
		listImages.clear();
		Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_REL_ID,COLUMN_IMG_NAME,COLUMN_IS_FREE,COLUMN_MUSEUM}
										, COLUMN_REL_ID+"=? AND "+COLUMN_MUSEUM+"=? AND "+COLUMN_IS_FREE+" =? ORDER BY "+COLUMN_IMG_NAME,
										new String[]{relId,museum.getShortName(),String.valueOf(DBUtils.boolToInt(isFree))} 
									, null, null, null);
		if(c.moveToFirst()){
			while(!c.isAfterLast()){
				listImages.add(new Image(c));
				c.moveToNext();
			}			
		}
		c.close();
	}
	
	private static void initSelection(int columnLength, int selectionArgLength){
		if(columns==null||columns.length!=columnLength){
			columns = new String[columnLength];
		}
		
		if(selectionArg==null||selectionArg.length!=selectionArgLength){
			selectionArg = new String[selectionArgLength];
		}
		
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	public static class Image{
		private static final String TAG = Image.class.getName();
				
		
		private static final String DELIMITER = "-";
		
		private String  relId;
		private String  name;
		private boolean isFree;
		private Museum  museum;
				
		private static final ContentValues values = new ContentValues();
		
		public String getRelId() {
			return relId;
		}

		public void setRelId(String relId) {
			this.relId = relId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isFree() {
			return isFree;
		}

		public void setFree(boolean isFree) {
			this.isFree = isFree;
		}

		public Museum getMuseum() {
			return museum;
		}

		public void setMuseum(Museum museum) {
			this.museum = museum;
		}
		
		public Image(Museum museum,File f){

//			}
			String fileName = f.getName();
			String[] fileNameParts = fileName.split(DELIMITER);
			if(fileNameParts!=null&&fileNameParts[0]!=null){
				relId = fileNameParts[0];
				name = fileName;
			}
			
			this.museum = museum;
//			scanner.close();
		}
		
		public Image(Cursor c) {
			relId  = c.getString(c.getColumnIndex(COLUMN_REL_ID));
 			name   = c.getString(c.getColumnIndex(COLUMN_IMG_NAME));
			isFree = DBUtils.intToBool(c.getInt(c.getColumnIndex(COLUMN_IS_FREE)));
			museum = Museum.getMuseum(c.getString(c.getColumnIndex(COLUMN_MUSEUM)));
		}
		
		public ContentValues getContentValues(){
			values.clear();
			values.put(COLUMN_REL_ID,relId);
			values.put(COLUMN_IMG_NAME, name);
			values.put(COLUMN_IS_FREE, DBUtils.boolToInt(isFree));
			values.put(COLUMN_MUSEUM, museum.getShortName());
			return values;
		}

		
	}
	
	

	
}
