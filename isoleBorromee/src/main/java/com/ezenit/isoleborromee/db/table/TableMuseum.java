package com.ezenit.isoleborromee.db.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.ezenit.isoleborromee.R;

public class TableMuseum {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String TABLE_NAME = "museum";
	
	private static final String COLUMN_ID   	  = "_id";
	private static final String COLUMN_SHORT_NAME = "s_name";
	private static final String COLUMN_NAME 	  = "name";
	private static final String COLUMN_DESC 	  = "desc";
	private static final String COLUMN_SKU 	  	  = "sku";
	
	
	private static final String CREATE_TABLE = "CREATE "+TABLE_NAME+" IF NOT EXISTS ("
														+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
														+COLUMN_SHORT_NAME+" TEXT, "
														+COLUMN_DESC+" TEXT, "
														+COLUMN_SKU+" TEXT, "
														+COLUMN_NAME+" TEXT)";
	
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
	public static void onCreate(SQLiteDatabase db){
		db.execSQL(CREATE_TABLE);
		createOrUpdateMuseum(db, Museum.MUSEUM_ISOLA_BELLA);
		createOrUpdateMuseum(db, Museum.MUSEUM_ISOLA_MADRE);
		createOrUpdateMuseum(db, Museum.MUSEUM_ROCA_D_ANGERA);
	}
	
	public static void onUpgrade(int oldVersion,int newVersion, SQLiteDatabase db){
		
	}
	// ===========================================================
	// Methods
	// ===========================================================
	
	
	public static void createOrUpdateMuseum(SQLiteDatabase db, Museum museum){
		
		if(museum==null){		
			throw new IllegalArgumentException("Museum cannot be null or empty");
		}
		String selection = COLUMN_SHORT_NAME+"=?";
		String[] selectionArg = new String[]{museum.getShortName()};
		Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_ID}, selection
				,selectionArg, null, null, null);
		
		if(c.moveToFirst()){
			db.insert(TABLE_NAME, null, museum.getContentValues());
		}
		else{
			db.update(TABLE_NAME, museum.getContentValues(), selection, selectionArg);
		}
		c.close();
				
	}
	


	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	public static class Museum implements Parcelable{
		
		// ===========================================================
		// Constants
		// ===========================================================
						
		public static final String SH_ISOLA_BELLA   = "IB";
		public static final String SH_ISOLA_MADRE   = "IM";
		public static final String SH_ROCA_D_ANGERA = "RA"; 
		
		
		public static final Museum MUSEUM_ISOLA_BELLA = new Museum(1, R.string.isola_bella,R.string.isole_bela_desc
																	,SH_ISOLA_BELLA,"isole-bella","isole_bela",R.drawable.isolebella_bg);
		public static final Museum MUSEUM_ISOLA_MADRE = new Museum(2, R.string.isola_madre,R.string.isole_madre_desc
																	, SH_ISOLA_MADRE,"isole-madre","isole_madre",R.drawable.isolamadre_bg);
		public static final Museum MUSEUM_ROCA_D_ANGERA = new Museum(3, R.string.rocca_d_angera,R.string.rocca_d_angera_desc
																	, SH_ROCA_D_ANGERA,"rocca-angera","rocca_d_angera",R.drawable.roccadiangera_bg);
		
		public static final Museum MUSEUM_ALL_IN_ONE  = new Museum(4, R.string.all_in_one,R.string.rocca_d_angera_desc
				, "AIO","all_in_one","all_in_one",R.drawable.isole_bg);
		
		protected final static ContentValues values = new ContentValues();
		
	
		
		// ===========================================================
		// Fields
		// ===========================================================
		private final long   id;
		private final String sName;
		private final int    nameId;
		private final int 	 descId;
		private final String folderName;
		private final String sku;
		private final int	 coverImageId;
		
		// ===========================================================
		// Constructors
		// ===========================================================
		
		public static final Parcelable.Creator<Museum> CREATOR
		        = new Parcelable.Creator<Museum>() {
		    public Museum createFromParcel(Parcel in) {
		        return new Museum(in);
		    }
		
		    public Museum[] newArray(int size) {
		        return new Museum[size];
		    }
		};

		public static Museum[] getAllMuseums(){
			return new Museum[]{MUSEUM_ISOLA_BELLA,MUSEUM_ISOLA_MADRE,MUSEUM_ROCA_D_ANGERA};
		}
		
		public static Museum[] getAllSkus(){
			return new Museum[]{MUSEUM_ISOLA_BELLA,MUSEUM_ISOLA_MADRE,MUSEUM_ROCA_D_ANGERA,MUSEUM_ALL_IN_ONE};
		}
		
		public Museum(long id,int nameId,int descId,String shortName,String folderName,String sku,int coverImageId){
			this.id = id;
			this.nameId = nameId;
			this.descId = descId;
			this.sName = shortName;
			this.folderName = folderName;
			this.sku	= sku;
			this.coverImageId = coverImageId;
		}
		
		public Museum(String folderName,Cursor c,int coverImageId){
			this.id = c.getLong(c.getColumnIndex(COLUMN_ID));
			this.sName = c.getString(c.getColumnIndex(COLUMN_SHORT_NAME));
			this.nameId = c.getInt(c.getColumnIndex(COLUMN_NAME));
			this.descId = c.getInt(c.getColumnIndex(COLUMN_DESC));
			this.folderName = folderName;
			this.sku	= c.getString(c.getColumnIndex(COLUMN_SKU));
			this.coverImageId = coverImageId;
			
		}
		
		private Museum(Parcel in){
			this.id 		= in.readLong();
			this.nameId 	= in.readInt();
			this.descId		= in.readInt();
			this.sName  	= in.readString();
			this.folderName = in.readString();
			this.sku		= in.readString();
			this.coverImageId = in.readInt();
		}
		// ===========================================================
		// Getter & Setter
		// ===========================================================

		
		

		public long getId() {
			return id;
		}
		
		public String getFolderName() {
			return folderName;
		}

		
		
		public String getShortName() {
			return sName;
		}

		public String getSku() {
//			return "android.test.purchased";
			return sku;
		}
		

		public int getNameId() {
			return nameId;
		}

		public int getDescId() {
			return descId;
		}
		
		public int getCoverImageId() {
			return coverImageId;
		}
		
		public ContentValues getContentValues(){
			values.clear();
			
			values.put(COLUMN_ID, id);
			values.put(COLUMN_NAME, nameId);
			values.put(COLUMN_SHORT_NAME, sName);			
			
			return values;
		}
		
		public static Museum getMuseum(String shortName){
			if(shortName.equals(SH_ISOLA_BELLA)){
				return MUSEUM_ISOLA_BELLA;
			}
			else if(shortName.equals(SH_ISOLA_MADRE)){
				return MUSEUM_ISOLA_MADRE;
			}
			else if(shortName.equals(SH_ROCA_D_ANGERA)){
				return MUSEUM_ROCA_D_ANGERA;
			}
			return null;
		}
		
		public static Museum getMuseum(long selectedmusuem) {
			if(selectedmusuem==1){
				return MUSEUM_ISOLA_BELLA;
			}
			else if(selectedmusuem==2){
				return MUSEUM_ISOLA_MADRE;
			}
			else if(selectedmusuem == 3){
				return MUSEUM_ROCA_D_ANGERA;
			}
			return null;
			
		}
		
		// ===========================================================
		// Methods for/from SuperClass/Interfaces
		// ===========================================================
		
		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if(o instanceof Museum){
				return sName.equals(((Museum)o).getShortName());
			}
			return false;
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeLong(id);
			dest.writeInt(nameId);
			dest.writeInt(descId);
			dest.writeString(sName);
			dest.writeString(folderName);
			dest.writeString(sku);
			dest.writeInt(coverImageId);
		}

		
		
		// ===========================================================
		// Inner and Anonymous Classes
		// ===========================================================
	
	}
}
