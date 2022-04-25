package com.ezenit.isoleborromee.db.table;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.model.IImage;

public class TablePhotoGallery {
	// ===========================================================
	// Constants
	// ===========================================================
	public static enum  RelationType{TYPE_GALLERY,TYPE_FREE_GUIDE,TYPE_PAID_GUIDE};
	
	private static final String TABLE_NAME = "photo_gallery";
		
	private static final String COLUMN_ID  	    = "id";
	private static final String COLUMN_IMG_CODE = "img_code";
	private static final String COLUMN_IMG_DESC = "img_desc";
	private static final String COLUMN_IMG_TITLE= "img_title";
	private static final String COLUMN_MUSEUM   = "museum";
	private static final String COLUMN_LANGUAGE = "language";
	
	private static final String CREATE_TABLE 	= "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("
														+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
														+COLUMN_IMG_CODE+" TEXT, "
														+COLUMN_IMG_DESC+" TEXT, "
														+COLUMN_IMG_TITLE+" TEXT, "
														+COLUMN_MUSEUM+" TEXT, " 
														+COLUMN_LANGUAGE+" TEXT)";
	
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

	// ===========================================================
	// Methods
	// ===========================================================
	
	public static void onCreate(SQLiteDatabase db){
		db.execSQL(CREATE_TABLE);
	}
	
	public static void onUpgrade(int oldVersion,int newVersion,SQLiteDatabase db){
		
	}
	
	public static ArrayList<GalleryImage> getGalleryImages(SQLiteDatabase db,Museum musuem,String languageShort){
		ArrayList<GalleryImage> galleryImages = new ArrayList<GalleryImage>();
		populateImages(db, musuem, languageShort, galleryImages);
		return galleryImages;
	}
	
	public static void populateImages(SQLiteDatabase db, Museum museum, String language, ArrayList<GalleryImage> images){
		images.clear();
		Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_ID,COLUMN_IMG_CODE,COLUMN_IMG_DESC
							,COLUMN_IMG_TITLE}
							, COLUMN_MUSEUM+" =? AND "+COLUMN_LANGUAGE+" =?"
							, new String[]{museum.getShortName(),language}, null, null, null);
		if(c.moveToFirst()){
			while(!c.isAfterLast()){
				images.add(new GalleryImage(c,museum, language));
				c.moveToNext();
			}
		}
		c.close();
	}
	
	public static void createOrUpdate(SQLiteDatabase db,GalleryImage image){
		initSelection(1, 3);
		columns[0]	= COLUMN_ID;
		selectionArg[0] = image.getCode();
		selectionArg[1] = image.getMuseumId();
		selectionArg[2] = image.getLanguage();
		String selection = COLUMN_IMG_CODE+"=? AND "+COLUMN_MUSEUM+" =? AND "+COLUMN_LANGUAGE+" = ?";
		Cursor c = db.query(TABLE_NAME,columns, selection, selectionArg, null, null, null);
		if(c.moveToFirst()){
			db.update(TABLE_NAME, image.getContentValues(),selection , selectionArg);
		}
		else{
			db.insert(TABLE_NAME, null, image.getContentValues());
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
	
	public static class GalleryImage implements IImage{
		// ===========================================================
		// Constants
		// ===========================================================
		private static final String IMG_EXTENSION = ".jpg";
		// ===========================================================
		// Fields
		// ===========================================================
		
		private long   id;	
		private String code;
		private String title;
		private String description;
		private Museum museum;
		private String language;
		
		protected final static ContentValues values = new ContentValues();
		
		public static final Parcelable.Creator<GalleryImage> CREATOR
		        = new Parcelable.Creator<GalleryImage>() {
		    public GalleryImage createFromParcel(Parcel in) {
		        return new GalleryImage(in);
		    }
		
		    public GalleryImage[] newArray(int size) {
		        return new GalleryImage[size];
		    }
		};
		
		// ===========================================================
		// Constructors
		// ===========================================================
		public GalleryImage(Museum museum,String languageShort) {
			this.museum = museum;
			this.language = languageShort;
		}
		
		public GalleryImage(Cursor c, Museum museum, String languageShort) {
			this.museum = museum;
			this.language = languageShort;
			this.id = c.getLong(c.getColumnIndex(COLUMN_ID));
			this.code = c.getString(c.getColumnIndex(COLUMN_IMG_CODE));
			this.title = c.getString(c.getColumnIndex(COLUMN_IMG_TITLE));
			this.description = c.getString(c.getColumnIndex(COLUMN_IMG_DESC));
		}
		
		public GalleryImage(Parcel in) {
			this.museum   	 = in.readParcelable(Museum.class.getClassLoader());
			this.language 	 = in.readString();
			this.id		  	 = in.readLong();
			this.code	  	 = in.readString();
			this.title	  	 = in.readString();
			this.description = in.readString();
		}
		
		// ===========================================================
		// Getter & Setter
		// ===========================================================
		
		

		public ContentValues getContentValues(){
			values.clear();
			
			values.put(COLUMN_IMG_CODE, code);
			values.put(COLUMN_IMG_TITLE, title);
			values.put(COLUMN_IMG_DESC, description);
			values.put(COLUMN_MUSEUM, museum.getShortName());
			values.put(COLUMN_LANGUAGE, language);
			
			return values;
		}
		
		
		
		public void setTitle(String title) {
			this.title = title;
		}
		
		public void setDescription(String description) {
			this.description = description;
		}
		
		public String getLanguage() {
			return language;
		}
		
		public long getId() {
			return id;
		}
		
		public void setCode(String code) {
			this.code = code;
		}
		
		public String getCode() {
			return code;
		}
		
		public String getMuseumId(){
			return museum.getShortName();
		}
		
		// ===========================================================
		// Methods for/from SuperClass/Interfaces
		// ===========================================================
		
		@Override
		public String getTitle() {
			return title;
		}
		
		@Override
		public String getImgName() {
			// TODO Auto-generated method stub
			return code+IMG_EXTENSION;
		}
		
		@Override
		public String getDescrition() {
			// TODO Auto-generated method stub
			return description;
		}
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeParcelable(museum, flags);
			dest.writeString(language);
			dest.writeLong(id);
			dest.writeString(code);
			dest.writeString(title);
			dest.writeString(description);
		}

		// ===========================================================
		// Methods
		// ===========================================================

		// ===========================================================
		// Inner and Anonymous Classes
		// ===========================================================
	}
}
