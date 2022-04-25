package com.ezenit.isoleborromee.db.table;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.ezenit.db.DBTable;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;

public class TableMap extends DBTable{
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String TABLE_NAME		= "map";

	public static final String COLUMN_ID 		= "_id";
	public static final String COLUMN_ROOM_NO	= "room_no";
	public static final String COLUMN_COORDS	= "coords";
	public static final String COLUMN_MUSEUM    = "museum";
	public static final String COLUMN_SECTION	= "section";
	public static final String COLUMN_CIRCLE_COORDS = "circle_coords";

	public static final String CREATE_TABLE 	= "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("
													+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
													+COLUMN_ROOM_NO+" TEXT, "
													+COLUMN_COORDS+" TEXT, "
													+COLUMN_CIRCLE_COORDS+" TEXT, "
													+COLUMN_MUSEUM+" TEXT, "
													+COLUMN_SECTION+" INTEGER)";

	// ===========================================================
	// Fields
	// ===========================================================

	private static String[] selectionArg;
	private static String[] columns;

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

	public static void onUpgrade(int oldVersion, int newVersion,SQLiteDatabase db){

	}



	// ===========================================================
	// Methods
	// ===========================================================
	public static void createOrUpdate(Room roomCoordinates) {
		initSelection(1, 4);
		columns[0]		 = COLUMN_ROOM_NO;
		selectionArg[0]  = String.valueOf(roomCoordinates.getRoomNo());
		selectionArg[1]  = roomCoordinates.getMusuem().getShortName();
		selectionArg[2]  = String.valueOf(roomCoordinates.getSection());
		selectionArg[3]  = String.valueOf(roomCoordinates.getCircleCoords());
		if(!TextUtils.isEmpty(selectionArg[0])&&!TextUtils.isEmpty(selectionArg[1])){
			String selection = COLUMN_ROOM_NO+"=? AND "+COLUMN_MUSEUM+" =? AND "+COLUMN_SECTION+" =? AND "+COLUMN_CIRCLE_COORDS+" =?";
			Cursor c = getDB().query(TABLE_NAME,columns, selection, selectionArg, null, null, null);
			if(c.moveToFirst()){
				c.close();
				getDB().update(TABLE_NAME, roomCoordinates.getContentValues(),selection , selectionArg);
			}
			else{
				c.close();
				getDB().insert(TABLE_NAME, null, roomCoordinates.getContentValues());
			}

		}

	}

	public static ArrayList<Room> getAllRooms(Museum museum,int section){
		ArrayList<Room> rooms = new ArrayList<TableMap.Room>();
		populateAllRooms(museum, section, rooms);
		return rooms;
	}

	public static void populateAllRooms(Museum museum,int section,ArrayList<Room> rooms){
		Cursor c = getDB().query(TABLE_NAME, new String[]{COLUMN_ID
											,COLUMN_ROOM_NO
											,COLUMN_COORDS
											,COLUMN_CIRCLE_COORDS}
							, COLUMN_MUSEUM+"=? AND "+COLUMN_SECTION+"=?", new String[]{museum.getShortName(),String.valueOf(section)}
			, null, null, null);
		rooms.clear();
		if(c.moveToFirst()){
			while(!c.isAfterLast()){
				rooms.add(new Room(c,museum, section));
				c.moveToNext();
			}
		}

	}

	public static Room getRoom(long id){
		Cursor c = getDB().query(TABLE_NAME, new String[]{COLUMN_ID
					,COLUMN_MUSEUM
					,COLUMN_ROOM_NO
					,COLUMN_COORDS
					,COLUMN_SECTION
					,COLUMN_CIRCLE_COORDS}
					, COLUMN_ID+" = ?"
					, new String[]{String.valueOf(id)}
					, null, null, null);
		Room room = null;
		if(c.moveToFirst()){
		while(!c.isAfterLast()){
			room = new Room(c);
			c.moveToNext();
		}
		}
		c.close();
		return room;
	}

	public static ArrayList<Room> getRooms(Room room){
		return getRooms(room.getRoomNo(), room.getMusuem());
	}

	public static ArrayList<Room> getRooms(String roomNo,Museum museum){
		Cursor c = getDB().query(TABLE_NAME, new String[]{COLUMN_ID
											,COLUMN_ROOM_NO
											,COLUMN_COORDS
											,COLUMN_SECTION
											,COLUMN_CIRCLE_COORDS}
					, COLUMN_ROOM_NO+" = ? AND "+COLUMN_MUSEUM+" = ?"
					, new String[]{String.valueOf(roomNo),museum.getShortName()}
					, null, null, null);

		ArrayList<Room> rooms = new ArrayList<TableMap.Room>();
		if(c.moveToFirst()){
			while(!c.isAfterLast()){
				rooms.add(new Room(c, museum));
				c.moveToNext();
			}
		}
		c.close();
		return rooms;
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
	public static class Room{
		private long 	id;
		private String 	roomNo;
		private String 	coords;
		private Museum  musuem;
		private int		section;
		private String  circleCoords;

		protected ContentValues values = new ContentValues();

		public Room(Cursor c,Museum musuem, int section) {
			this.id	    = c.getLong(c.getColumnIndex(COLUMN_ID));
			this.roomNo = c.getString(c.getColumnIndex(COLUMN_ROOM_NO));
			this.coords = c.getString(c.getColumnIndex(COLUMN_COORDS));
			this.circleCoords = c.getString(c.getColumnIndex(COLUMN_CIRCLE_COORDS));
			this.musuem = musuem;
			this.section= section;
		}

		public Room(Cursor c,Museum musuem) {
			this.id	    = c.getLong(c.getColumnIndex(COLUMN_ID));
			this.roomNo = c.getString(c.getColumnIndex(COLUMN_ROOM_NO));
			this.coords = c.getString(c.getColumnIndex(COLUMN_COORDS));
			this.circleCoords = c.getString(c.getColumnIndex(COLUMN_CIRCLE_COORDS));
			this.musuem = musuem;
			this.section= c.getInt(c.getColumnIndex(COLUMN_SECTION));
		}

		public Room(Cursor c) {
			this.id	    = c.getLong(c.getColumnIndex(COLUMN_ID));
			this.roomNo = c.getString(c.getColumnIndex(COLUMN_ROOM_NO));
			this.coords = c.getString(c.getColumnIndex(COLUMN_COORDS));
			this.circleCoords = c.getString(c.getColumnIndex(COLUMN_CIRCLE_COORDS));
			this.musuem = Museum.getMuseum(c.getString(c.getColumnIndex(COLUMN_MUSEUM)));
			this.section= c.getInt(c.getColumnIndex(COLUMN_SECTION));
		}

		public Room(Museum musuem, int section) {
			this.musuem = musuem;
			this.section = section;
		}

		public long getId() {
			return id;
		}

		public void setRoomNo(String roomNo) {

			this.roomNo = (roomNo);
		}

		/*public void setRoomNo(int roomNo) {
			this.roomNo = roomNo;
		}
*/
		public void setCoords(String coords) {
			this.coords = coords;
		}

		public void setCircleCoords(String circleCoords) {
			this.circleCoords = circleCoords;
		}

		public String getCircleCoords() {
			return circleCoords;
		}

		public String getRoomNo() {
			return roomNo;
		}

		public String getCoords() {
			return coords;
		}

		public void setMusuem(Museum musuem) {
			this.musuem = musuem;
		}

		public Museum getMusuem() {
			return musuem;
		}

		public void setSection(int section) {
			this.section = section;
		}

		public int getSection() {
			return section;
		}

		public ContentValues getContentValues(){
			values.clear();

			values.put(COLUMN_ROOM_NO, roomNo);
			values.put(COLUMN_MUSEUM, musuem.getShortName());
			values.put(COLUMN_COORDS, coords);
			values.put(COLUMN_SECTION, section);
			values.put(COLUMN_CIRCLE_COORDS, circleCoords);

			return values;
		}
	}


}
