package com.ezenit.isoleborromee.db.table;

import java.util.ArrayList;

import com.ezenit.db.DBTable;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TableSection extends DBTable {
	// ===========================================================
	// Constants
	// ===========================================================
	
	private static final String TABLE_NAME = "section";
	
	private static final String COLUMN_ID   	  = "_id";
	private static final String COLUMN_SERVER_ID  = "server_id";
	private static final String COLUMN_NAME 	  = "name";
	private static final String COLUMN_MUSEUM 	  = "museum";
	private static final String COLUMN_LANGUAGE   = "lang";
	
	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("
														+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
														+COLUMN_SERVER_ID+" INTEGER, "
														+COLUMN_NAME+" TEXT, "
														+COLUMN_LANGUAGE+" TEXT, "
														+COLUMN_MUSEUM+" TEXT )";
	
	
	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================
	public static void onCreate(SQLiteDatabase db){
		db.execSQL(CREATE_TABLE);
	}
	
	public static void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
		
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	public static void insertOrUpdate(Section section){
		String selection = COLUMN_MUSEUM+"= ? AND "+COLUMN_SERVER_ID+" =? AND "+COLUMN_LANGUAGE+" = ?";
		String[] selectionArgs = new String[]{section.museum.getShortName(),String.valueOf(section.id),section.language};
		Cursor c = getDB().query(TABLE_NAME, new String[]{COLUMN_SERVER_ID}, selection, selectionArgs, null, null, null);
		if(c.moveToFirst()){
			c.close();
			getDB().update(TABLE_NAME, section.getValues(), selection, selectionArgs);
		}
		else{
			c.close();
			getDB().insert(TABLE_NAME, null, section.getValues());
		}
	}
	
	public static Section getSection(int id,Museum museum,String language){
		String selection = COLUMN_MUSEUM+"= ? AND "+COLUMN_SERVER_ID+" =? AND "+COLUMN_LANGUAGE+" = ?";
		String[] selectionArgs = new String[]{museum.getShortName(),String.valueOf(id), language};
		Cursor c = getDB().query(TABLE_NAME, new String[]{COLUMN_NAME,COLUMN_LANGUAGE}, selection, selectionArgs, null, null, null);
		Section section = null;
		if(c.moveToFirst()){
			section = new Section(id, museum, c);
		}
		c.close();
		return section;
	}
	
	public static ArrayList<Section> getSections(Museum museum, String language){
		String selection = COLUMN_MUSEUM+"= ? AND "+COLUMN_LANGUAGE+" = ?";
		String[] selectionArgs = new String[]{museum.getShortName(),language};
		Cursor c = getDB().query(TABLE_NAME, new String[]{COLUMN_NAME,COLUMN_SERVER_ID
				,COLUMN_LANGUAGE}, selection, selectionArgs, null, null, null);
		ArrayList<Section> sections = new ArrayList<TableSection.Section>();
		if(c.moveToFirst()){
			
			while(!c.isAfterLast()){
				sections.add(new Section(museum, c));
				c.moveToNext();
			}
		}
		c.close();
		return sections;
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static class Section{
		
		// ===========================================================
		// Constants
		// ===========================================================
		// ===========================================================
		// Fields
		// ===========================================================

		private int 	id;
		private String 	name;
		private Museum  museum;
		private String  language;
		
		private static final ContentValues values = new ContentValues();
		
		// ===========================================================
		// Constructors
		// ===========================================================
		
		public Section() {
			
		}
		
		public Section(int id, Museum museum,Cursor c){
			this(id,museum,c.getString(c.getColumnIndex(COLUMN_NAME))
					,c.getString(c.getColumnIndex(COLUMN_LANGUAGE)));
		}
		
		public Section(Museum museum,Cursor c){
			this(c.getInt(c.getColumnIndex(COLUMN_SERVER_ID))
					,museum
					,c.getString(c.getColumnIndex(COLUMN_NAME))
					,c.getString(c.getColumnIndex(COLUMN_LANGUAGE)));
					
		}
		
		public Section(Cursor c){
			this(c.getInt(c.getColumnIndex(COLUMN_SERVER_ID))
					, Museum.getMuseum(c.getString(c.getColumnIndex(COLUMN_MUSEUM)))
					, c.getString(c.getColumnIndex(COLUMN_NAME))
					,c.getString(c.getColumnIndex(COLUMN_LANGUAGE)));
		}
		
		public Section(int id, Museum museum,String name,String language){
			this.id		 = id;
			this.name 	 = name;
			this.museum  = museum;
			this.language = language;
		}
		
		// ===========================================================
		// Getter & Setter
		// ===========================================================
		
		public void setId(int id) {
			this.id = id;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public void setMuseum(Museum museum) {
			this.museum = museum;
		}
		
		public int getId() {
			return id;
		}
		
		public String getName() {
			return name;
		}
		
		public Museum getMuseum() {
			return museum;
		}
		
		public ContentValues getValues(){
			values.put(COLUMN_SERVER_ID, id);
			values.put(COLUMN_NAME, name);
			values.put(COLUMN_MUSEUM, museum.getShortName());
			values.put(COLUMN_LANGUAGE, language);
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
