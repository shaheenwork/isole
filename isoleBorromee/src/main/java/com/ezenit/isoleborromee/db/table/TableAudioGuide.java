package com.ezenit.isoleborromee.db.table;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.ezenit.db.DBTable;
import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.db.DBUtils;
import com.ezenit.isoleborromee.db.table.TableImgRel.Image;
import com.ezenit.isoleborromee.db.table.TableMap.Room;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

public class TableAudioGuide extends DBTable {
    // ===========================================================
    // Constants
    // ===========================================================
    private static final String TABLE_NAME = "audio";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CODE_NO = "code_no";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESC = "desc";
    private static final String COLUMN_IS_FREE = "is_free";
    private static final String COLUMN_LANGUAGE = "lang";
    private static final String COLUMN_ROOM = "room";
    private static final String COLUMN_MUSEUM = "museum";


    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_CODE_NO + " TEXT, "
            + COLUMN_TITLE + " TEXT, "
            + COLUMN_DESC + " TEXT, "
            + COLUMN_IS_FREE + " INTEGER, "
            + COLUMN_LANGUAGE + " TEXT" + ", "
            + COLUMN_ROOM + " TEXT, "
            + COLUMN_MUSEUM + " TEXT)";

    private static String[] columns;
    private static String[] selectionArg;
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
        db.execSQL(CREATE_TABLE);
    }

    public static void onUpgrade(int oldVersion, int newVersion, SQLiteDatabase db) {
        // TODO Auto-generated method stub

    }


    // ===========================================================
    // Methods
    // ===========================================================


    /*****************************************************
     * If the audio guide code exists updates that specific record,
     * else creates a new record in app db.
     *
     * param db instance of App Database
     * @param audioGuide the audio guide record to be inserted
     *
     * ***************************************************/
    public static void createOrUpdate(AudioGuide audioGuide) {
        initSelection(1, 4);
        columns[0] = COLUMN_CODE_NO;
        selectionArg[0] = audioGuide.getCodeNo();
        selectionArg[1] = audioGuide.getLanguageShort();
        selectionArg[2] = audioGuide.getMuseum().getShortName();
        selectionArg[3] = audioGuide.getDescription();
        String selection = COLUMN_CODE_NO + "=? AND " + COLUMN_LANGUAGE + " =?" + " AND " + COLUMN_MUSEUM + " = ? AND " + COLUMN_DESC + " = ?";

        SQLiteDatabase db = getDB();

        Cursor c = db.query(TABLE_NAME, columns, selection, selectionArg, null, null, null);
        if (c.moveToFirst()) {
            c.close();
            db.update(TABLE_NAME, audioGuide.getContentValues(), selection, selectionArg);
        } else {
            c.close();
            db.insert(TABLE_NAME, null, audioGuide.getContentValues());
        }

    }


    public static void populateAudioGuides(Museum museum, String language
            , boolean isFree, ArrayList<AudioGuide> listAudioGuide) {
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("Language cannot be null or empty");
        }

        listAudioGuide.clear();

        SQLiteDatabase db = getDB();

        Cursor c = db.rawQuery("SELECT " + COLUMN_ID + ","
                        + COLUMN_CODE_NO + ","
                        + COLUMN_TITLE + ","
                        + COLUMN_DESC + ","
                        + COLUMN_ROOM
                        + " FROM " + TABLE_NAME
                        + " WHERE " + COLUMN_LANGUAGE + "=?"
                        + " AND " + COLUMN_MUSEUM + "=?"
                        + " AND " + COLUMN_IS_FREE + "=?"
                , new String[]{language, museum.getShortName(), String.valueOf(DBUtils.boolToInt(isFree))});
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                AudioGuide guide = new AudioGuide(museum, language, isFree, c);
                if (!guide.isParent())
                    listAudioGuide.add(guide);
                c.moveToNext();
            }
        }
        c.close();
    }


    /**********************************************************************
     * Query the application db and return all Audio guides base information
     * as {@link ArrayList} of { BAudioGuide}
     * related to a particular language
     *
     *  db instance of application db
     * @param language language of the audio content
     * @param museum  shortcut name of museum, can be
     * 					<br/>{ MuseumConstants#SH_ISOLA_BELLA}
     * 					<br/>{ MuseumConstants#SH_ISOLA_MADRE}
     * 					<br/>{ MuseumConstants#SH_ROCA_D_ANGERA}
     *
     * @return all the audio guides as {@link ArrayList} of { BAudioGuide}
     * ********************************************************************/
    public static ArrayList<BaseAudioGuide> getAllAudioGuides(String language, Museum museum, boolean isFree) {
        ArrayList<BaseAudioGuide> listAudioGuide = new ArrayList<TableAudioGuide.BaseAudioGuide>();
        getAllAudioGuides(listAudioGuide, museum, language, isFree);
        return listAudioGuide;
    }

    public static AudioGuide getRandomGuide() {
        SQLiteDatabase db = getDB();


        Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_ID,
                        COLUMN_CODE_NO, COLUMN_DESC,
                        COLUMN_LANGUAGE, COLUMN_MUSEUM, COLUMN_ROOM,
                        COLUMN_IS_FREE, COLUMN_TITLE},
                null, null, null, null, "RANDOM()", "1");
        AudioGuide audioGuide = null;
        if (c.moveToFirst()) {
            audioGuide = new AudioGuide(c);
        }
        c.close();
        return audioGuide;

    }

    /**********************************************************************
     * Query the application db populates all the audio guides
     * basic details to the given arraylist
     * related to a particular language
     *
     * @param listAudioGuide instance of {@link ArrayList}
     * 		 of { BAudioGuide} to be populated
     * @param museum  shortcut name of museum, can be
     * 					<br/>{ MuseumConstants#SH_ISOLA_BELLA}
     * 					<br/>{ MuseumConstants#SH_ISOLA_MADRE}
     * 					<br/>{ MuseumConstants#SH_ROCA_D_ANGERA}
     * @param language language of the audio content
     * @param isFree       if the content is free
     *
     * ********************************************************************/
    public static void getAllAudioGuides(ArrayList<BaseAudioGuide> listAudioGuide
            , Museum museum, String language, boolean isFree) {
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("Language cannot be null or empty");
        }

        listAudioGuide.clear();

        SQLiteDatabase db = getDB();

        Cursor c = db.rawQuery("SELECT " + COLUMN_ID + ","
                        + COLUMN_CODE_NO + ","
                        + COLUMN_TITLE + ","
                        + COLUMN_ROOM
                        + " FROM " + TABLE_NAME
                        + " WHERE " + COLUMN_LANGUAGE + "=?"
                        + " AND " + COLUMN_MUSEUM + "=?"
                        + " AND " + COLUMN_IS_FREE + "=?"
                , new String[]{language, museum.getShortName(), String.valueOf(DBUtils.boolToInt(isFree))});
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                listAudioGuide.add(new BaseAudioGuide(museum, language, isFree, c));
                c.moveToNext();
            }
        }
        c.close();
    }

    public static ArrayList<TopBaseAudioGuide> getTopAudioGuides(String langShort,
                                                                 Museum museum, boolean isFree) {
        ArrayList<TopBaseAudioGuide> audioGuides = new ArrayList<TopBaseAudioGuide>();
        populateTopAudioGuides(audioGuides, museum, langShort, isFree);
        return audioGuides;
    }


//	SELECT *,MAX(desc='-1') FROM [audio] WHERE code_no GLOB '*[0-9]' GROUP BY code_no;


///changed by bibin we want to add 0P-9P regular experssion in here				

    public static String INVALID_DESC = "-1";

    ///////////
    public static void populateTopAudioGuides(ArrayList<TopBaseAudioGuide> listAudioGuide
            , Museum museum, String language, boolean isFree) {

        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("Language cannot be null or empty");
        }

        listAudioGuide.clear();

        SQLiteDatabase db = getDB();

        Cursor c = db.rawQuery("SELECT " + COLUMN_ID + ","
                        + COLUMN_CODE_NO + ","
                        + COLUMN_TITLE + ","
                        + COLUMN_ROOM + ","
                        + COLUMN_DESC
                        + " FROM " + TABLE_NAME
                        + " WHERE " + COLUMN_LANGUAGE + "=?"
                        + " AND " + COLUMN_MUSEUM + "=?"
                        + " AND " + COLUMN_IS_FREE + "=?"
                        + " AND " + COLUMN_CODE_NO + " GLOB ?"
                //change by bibin ORDER BY commented
               //  + " ORDER BY " + COLUMN_CODE_NO
                , new String[]{language, museum.getShortName()
                        , String.valueOf(DBUtils.boolToInt(isFree)), "*[0-9,0P-9P,0G-9G]"});

        ArrayList<String> parentCodes = getAllParentCodes(language, museum, isFree);
        Log.d(TAG, "populateTopAudioGuides: " + parentCodes);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                TopBaseAudioGuide parentGuide = new TopBaseAudioGuide(museum, language, isFree, c);
                String desc = c.getString(c.getColumnIndex(COLUMN_DESC)).trim();
                Log.d(TAG, "populateTopAudioGuidesSize: " + parentCodes.size());

                if (parentCodes.contains(parentGuide.getCodeNo())) {
                    if (desc.equals(INVALID_DESC)) {
                        parentGuide.setChildAudioGuides(getChildAudioGuides(parentGuide));
                        listAudioGuide.add(parentGuide);
                    }
                } else
                    listAudioGuide.add(parentGuide);
                Log.d("bibin babu", "getAllParentCodes: " + parentCodes);
                c.moveToNext();
            }
        }
        c.close();
    }

    public static ArrayList<String> getAllParentCodes(String language, Museum museum, boolean isFree) {
        ArrayList<String> parentCodes = new ArrayList<String>();
        populateParentCode(parentCodes, language, museum, isFree);
        Log.d("bibin babu", "getAllParentCodes: " + parentCodes);
        return parentCodes;
    }


    ///changed by bibin we want to add 0P-9P regular experssion in here

    public static void populateParentCode(ArrayList<String> parentCodes, String language, Museum museum, boolean isFree) {

        SQLiteDatabase db = getDB();
        Log.d(TAG, "populateParentCode: " + parentCodes);
        Cursor c = db.rawQuery("SELECT " + COLUMN_CODE_NO + ","
                        + "COUNT (" + COLUMN_CODE_NO + ") c"
                        + " FROM " + TABLE_NAME
                        + " WHERE " + COLUMN_LANGUAGE + "=?"
                        + " AND " + COLUMN_MUSEUM + "=?"
                        + " AND " + COLUMN_IS_FREE + "=?"
                        + " AND " + COLUMN_CODE_NO + " GLOB ?"
                        + " GROUP BY " + COLUMN_CODE_NO
                        + " HAVING c >1"
                        + " ORDER BY " + COLUMN_CODE_NO + " ASC"
                , new String[]{language, museum.getShortName()

                        , String.valueOf(DBUtils.boolToInt(isFree)), "*[0-9,0P-9P,0G-9G]"});
        parentCodes.clear();
        if (c.moveToFirst()) {
            int columnCodeIndex = c.getColumnIndex(COLUMN_CODE_NO);
            while (!c.isAfterLast()) {
                parentCodes.add(c.getString(columnCodeIndex));
                c.moveToNext();
            }
        }
    }

    public static int getChildCount(String[] arg) {

        SQLiteDatabase db = getDB();

        Cursor c = db.rawQuery("SELECT " + COLUMN_ID
                + " FROM " + TABLE_NAME
                + " WHERE " + COLUMN_LANGUAGE + "=?"
                + " AND " + COLUMN_MUSEUM + "=?"
                + " AND " + COLUMN_IS_FREE + "=?"
                + " AND " + COLUMN_CODE_NO + " NOT GLOB ?", arg);
        return c.getCount();
    }

    public static ArrayList<BaseAudioGuide> getChildAudioGuides(TopBaseAudioGuide parentAudioGuide) {
        ArrayList<BaseAudioGuide> audioGuides = new ArrayList<TableAudioGuide.BaseAudioGuide>();
        populateChildAudioGuides(audioGuides, parentAudioGuide);
        return audioGuides;
    }

    public static void populateChildAudioGuides(ArrayList<BaseAudioGuide> audioGuides, TopBaseAudioGuide parentAudio) {
        initSelection(1, 5);

        selectionArg[0] = parentAudio.getLanguageShort();
        selectionArg[1] = parentAudio.museum.getShortName();
        selectionArg[2] = String.valueOf(DBUtils.boolToInt(parentAudio.isFree()));
        selectionArg[3] = parentAudio.getCodeNo() + "%";
        selectionArg[4] = INVALID_DESC;


        Cursor c = getDB().rawQuery("SELECT " + COLUMN_ID + ","
                        + COLUMN_CODE_NO + ","
                        + COLUMN_TITLE + ","
                        + COLUMN_ROOM
                        + " FROM " + TABLE_NAME
                        + " WHERE " + COLUMN_LANGUAGE + "=?"
                        + " AND " + COLUMN_MUSEUM + "=?"
                        + " AND " + COLUMN_IS_FREE + "=?"
                        + " AND " + COLUMN_CODE_NO + " LIKE ?"
                        + " AND " + COLUMN_DESC + " != ?"
                , selectionArg);
        audioGuides.clear();
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                audioGuides.add(new BaseAudioGuide(parentAudio.museum
                        , parentAudio.getLanguageShort(), parentAudio.isFree(), c));
                c.moveToNext();
            }
        }
        ;
    }

    private static final String TAG = TableAudioGuide.class.getName();

    public static BaseAudioGuide getBaseAudioGuide(long id) {

        Log.d(TAG, "getBaseAudioGuide: " + id);
        Cursor c = getDB().rawQuery("SELECT " + TABLE_NAME + "." + COLUMN_ID + ","
                + TABLE_NAME + "." + COLUMN_CODE_NO + ","
                + TABLE_NAME + "." + COLUMN_TITLE + ","
                + TABLE_NAME + "." + COLUMN_MUSEUM + ","
                + TABLE_NAME + "." + COLUMN_DESC + ","
                + TABLE_NAME + "." + COLUMN_LANGUAGE + ","
                + TABLE_NAME + "." + COLUMN_IS_FREE + ","
                + TABLE_NAME + "." + COLUMN_ROOM +
                " FROM " + TABLE_NAME +
                " WHERE " + COLUMN_ID + "= ?", new String[]{String.valueOf(id)});
        BaseAudioGuide audioGuide = null;
        if (c.moveToFirst()) {
            audioGuide = new BaseAudioGuide(c);
        }
        c.close();
        return audioGuide;
    }

    public static boolean isParent(long id) {
        Log.d(TAG, "isParent: " + id);
        Cursor c = getDB().rawQuery("SELECT " + TABLE_NAME + "." + COLUMN_ID + " " +
                        " FROM " + TABLE_NAME +
                        " WHERE " + COLUMN_DESC + "= ?" +
                        " AND " + COLUMN_ID + "= ?"
                , new String[]{INVALID_DESC
                        , String.valueOf(id)});
        boolean isParent = c.getCount() > 0;
        Log.d(TAG, "Isparent " + isParent);
        c.close();
        return isParent;
    }

    public static boolean hasChildren(BaseAudioGuide guide) {
        initSelection(1, 5);

        selectionArg[0] = guide.getLanguageShort();
        selectionArg[1] = guide.getMuseum().getShortName();
        selectionArg[2] = String.valueOf(DBUtils.boolToInt(guide.isFree()));
        selectionArg[3] = guide.getParentCode() + "%";
        selectionArg[4] = INVALID_DESC;


//		Log.d(TAG, "Parent code "+guide.getParentCode()+","+" IsFree "+guide.isFree());
        Cursor c = getDB().rawQuery("SELECT " + COLUMN_ID + ","
                        + COLUMN_CODE_NO + ","
                        + COLUMN_TITLE + ","
                        + COLUMN_ROOM
                        + " FROM " + TABLE_NAME
                        + " WHERE " + COLUMN_LANGUAGE + "=?"
                        + " AND " + COLUMN_MUSEUM + "=?"
                        + " AND " + COLUMN_IS_FREE + "=?"
                        + " AND " + COLUMN_CODE_NO + " LIKE ?"
                        + " AND " + COLUMN_DESC + " != ?"
                , selectionArg);
        boolean hasChildren = c.getCount() > 1;
        c.close();
//		Log.e(TAG, "Has Children "+hasChildren);
        return hasChildren;
    }

    public static boolean hasParent(BaseAudioGuide guide) {
        initSelection(1, 5);

        selectionArg[0] = guide.getLanguageShort();
        selectionArg[1] = guide.museum.getShortName();
        selectionArg[2] = String.valueOf(DBUtils.boolToInt(guide.isFree()));
        selectionArg[3] = "%" + guide.getParentCode() + "%";
        selectionArg[4] = INVALID_DESC;


        Cursor c = getDB().rawQuery("SELECT " + COLUMN_ID + ","
                        + COLUMN_CODE_NO + ","
                        + COLUMN_TITLE + ","
                        + COLUMN_ROOM
                        + " FROM " + TABLE_NAME
                        + " WHERE " + COLUMN_LANGUAGE + "=?"
                        + " AND " + COLUMN_MUSEUM + "=?"
                        + " AND " + COLUMN_IS_FREE + "=?"
                        + " AND " + COLUMN_CODE_NO + " LIKE ?"
                        + " AND " + COLUMN_DESC + " = ?"
                , selectionArg);
        boolean hasChildren = c.getCount() > 0;
        c.close();
        return hasChildren;
    }

    public static BaseAudioGuide getBaseAudioGuide(AudioGuide audioGuide) {
        return getBaseAudioGuide(audioGuide.getParentCode(), audioGuide.getLanguageShort());
    }

    public static BaseAudioGuide getBaseAudioGuide(String codeNo, String langShort) {

        Cursor c = getDB().rawQuery("SELECT " + TABLE_NAME + "." + COLUMN_ID + ","
                + TABLE_NAME + "." + COLUMN_CODE_NO + ","
                + TABLE_NAME + "." + COLUMN_TITLE + ","
                + TABLE_NAME + "." + COLUMN_MUSEUM + ","
                + TABLE_NAME + "." + COLUMN_DESC + ","
                + TABLE_NAME + "." + COLUMN_LANGUAGE + ","
                + TABLE_NAME + "." + COLUMN_IS_FREE + ","
                + TABLE_NAME + "." + COLUMN_ROOM +
                " FROM " + TABLE_NAME +
                " WHERE " + COLUMN_CODE_NO + "= ?"
                + " AND " + COLUMN_LANGUAGE + " =?", new String[]{codeNo, langShort});
        BaseAudioGuide audioGuide = null;
        if (c.moveToFirst()) {
            audioGuide = new BaseAudioGuide(c);
        }
        c.close();
        return audioGuide;
    }

    public static AudioGuide getAudioGuide(BaseAudioGuide baseAudioGuide) {
        return getAudioGuide(baseAudioGuide.getCodeNo()
                , baseAudioGuide.getLanguageShort()
                , baseAudioGuide.getMuseum()
                , baseAudioGuide.isFree());
    }

    /****************************************************************
     * Query the application db and retreive the audio guide
     * corresponding to the given id
     *
     * @param language language of the audio content
     * @param museum  shortcut name of museum, can be
     * 					<br/>{ MuseumConstants#SH_ISOLA_BELLA}
     * 					<br/>{ MuseumConstants#SH_ISOLA_MADRE}
     * 					<br/>{ MuseumConstants#SH_ROCA_D_ANGERA}
     *
     * @return {@link AudioGuide} if audio guide exists for
     * 		the given id, else null
     * ***************************************************************/
    public static AudioGuide getAudioGuide(String codeNo, String language, Museum museum, boolean isFree) {
        Cursor c = getDB().rawQuery("SELECT " + TABLE_NAME + "." + COLUMN_ID + ","
                        + TABLE_NAME + "." + COLUMN_CODE_NO + ","
                        + TABLE_NAME + "." + COLUMN_TITLE + ","
                        + TABLE_NAME + "." + COLUMN_DESC + ","
                        + TABLE_NAME + "." + COLUMN_ROOM +
                        " FROM " + TABLE_NAME +
                        " WHERE " + COLUMN_CODE_NO + "= ?"
                        + " AND " + COLUMN_LANGUAGE + "= ?"
                        + " AND " + COLUMN_MUSEUM + "=?"
                        + " AND " + COLUMN_IS_FREE + "=?"
                        + " AND " + COLUMN_DESC + " != ?"
                , new String[]{codeNo, language, museum.getShortName()
                        , String.valueOf(DBUtils.boolToInt(isFree))
                        , INVALID_DESC}
        );
        AudioGuide guide = null;

        if (c.moveToFirst()) {
            guide = new AudioGuide(museum, language, isFree, c);
            guide.setListPhoto(TableImgRel.getAllImages(getDB(), codeNo, museum, isFree));
        }

        c.close();
        return guide;
    }

    public static void populateAudioGuides(ArrayList<AudioGuide> listAudioGuide
            , String idList) {

        listAudioGuide.clear();

        Cursor c = getDB().rawQuery("SELECT " + TABLE_NAME + "." + COLUMN_ID + ","
                        + TABLE_NAME + "." + COLUMN_CODE_NO + ","
                        + TABLE_NAME + "." + COLUMN_TITLE + ","
                        + TABLE_NAME + "." + COLUMN_DESC + ","
                        + TABLE_NAME + "." + COLUMN_MUSEUM + ","
                        + TABLE_NAME + "." + COLUMN_LANGUAGE + ","
                        + TABLE_NAME + "." + COLUMN_IS_FREE + ","
                        + TABLE_NAME + "." + COLUMN_ROOM
                        + " FROM " + TABLE_NAME
                        + " WHERE " + COLUMN_ID + " IN (?)"
                , new String[]{idList});
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                listAudioGuide.add(new AudioGuide(c));
                c.moveToNext();
            }
        }
        c.close();
    }

    public static ArrayList<BaseAudioGuide> getAudioGuides(Room room, String language, boolean isFree) {
        ArrayList<BaseAudioGuide> guides = new ArrayList<TableAudioGuide.BaseAudioGuide>();
        populateAudioGuides(room, language, isFree, guides);
        return guides;
    }

/*

    ///bibin babu add G and P are for gargen and palace
    public static void populateAudioGuides(Room room, String language, boolean isFree, ArrayList<BaseAudioGuide> guides, int section) {
        Museum museum = room.getMusuem();
        Cursor c;
        Log.d(TAG, "populateAudioGuides: " + museum.getShortName());
        if (museum.getShortName().equals("IB")) {
            Log.d(TAG, "populateAudioGuides: " + room.getRoomNo());
            c = getDB().rawQuery("SELECT " + COLUMN_ID + ","
                            + COLUMN_CODE_NO + ","
                            + COLUMN_TITLE
                            + " FROM " + TABLE_NAME
                            + " WHERE " + COLUMN_ROOM + "=? AND "
                            + COLUMN_MUSEUM + " = ? AND "
                            + COLUMN_IS_FREE + " =? AND "
                            + COLUMN_LANGUAGE + " = ?"
                    , new String[]{String.valueOf(room.getRoomNo()),
                            museum.getShortName(),
                            String.valueOf(DBUtils.boolToInt(isFree)),
                            language});
        } else {
          //  Log.d(TAG, "populateAudioGuides: " + room.getRoomNo());
            String colum_code_experssion=null;
            if (section == 1) {
                colum_code_experssion = "*[0Paz-9Paz]";
            } else {
                colum_code_experssion = "*[0Gaz-9Gaz]";
            }
            c = getDB().rawQuery("SELECT " + COLUMN_ID + ","
                            + COLUMN_CODE_NO + ","
                            + COLUMN_TITLE
                            + " FROM " + TABLE_NAME
                            + " WHERE " + COLUMN_ROOM + "=? AND "
                            + COLUMN_MUSEUM + " = ? AND "
                            + COLUMN_IS_FREE + " =? AND "
                            + COLUMN_LANGUAGE + " = ?"
                            + " AND " + COLUMN_CODE_NO + " GLOB ?"

                    , new String[]{String.valueOf(room.getRoomNo()),
                            museum.getShortName(),
                            String.valueOf(DBUtils.boolToInt(isFree)),
                            language,colum_code_experssion});
        }
        guides.clear();
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                //   c.getColumnNames();

                BaseAudioGuide audioGuide = new BaseAudioGuide(museum, language, isFree, room, c);
                if (!isParent(audioGuide)) {
                    guides.add(audioGuide);
                }

                c.moveToNext();
            }
        }
        c.close();
    }

*/


    public static void populateAudioGuides(Room room, String language, boolean isFree, ArrayList<BaseAudioGuide> guides) {
        Museum museum = room.getMusuem();

        Log.d(TAG, "populateAudioGuides: " + museum.getShortName());
        Log.d(TAG, "populateAudioGuides: " + room.getRoomNo());
        Cursor c = getDB().rawQuery("SELECT " + COLUMN_ID + ","
                        + COLUMN_CODE_NO + ","
                        + COLUMN_TITLE
                        + " FROM " + TABLE_NAME
                        + " WHERE " + COLUMN_ROOM + "=? AND "
                        + COLUMN_MUSEUM + " = ? AND "
                        + COLUMN_IS_FREE + " =? AND "
                        + COLUMN_LANGUAGE + " = ?"
                , new String[]{String.valueOf(room.getRoomNo()),
                        museum.getShortName(),
                        String.valueOf(DBUtils.boolToInt(isFree)),
                        language});
        guides.clear();
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                //   c.getColumnNames();

                BaseAudioGuide audioGuide = new BaseAudioGuide(museum, language, isFree, room, c);
                if (!isParent(audioGuide)) {
                    guides.add(audioGuide);
                }

                c.moveToNext();
            }
        }
        c.close();
    }


    public static boolean isParent(BaseAudioGuide audioGuide) {
        return isParent(audioGuide.id);
    }

    private static void initSelection(int selectionLength, int selectionArgLength) {
        if (columns == null || columns.length != selectionLength) {
            columns = new String[selectionLength];
        }

        if (selectionArg == null || selectionArg.length != selectionArgLength) {
            selectionArg = new String[selectionArgLength];
        }

    }


    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    public static class AudioGuide extends BaseAudioGuide {

        private String description;
        private ArrayList<Image> listPhoto;

        public AudioGuide(Cursor c) {
            super(c);
            description = c.getString(c.getColumnIndex(COLUMN_DESC));

        }

        public AudioGuide(Museum museum, String language, boolean isFree, Cursor c) {
            super(museum, language, isFree, c);
            description = c.getString(c.getColumnIndex(COLUMN_DESC));

        }

        public AudioGuide(Museum museum, String language, boolean isFree) {
            super(museum, language, isFree);
        }


        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public ArrayList<Image> getListPhoto() {
            return listPhoto;
        }

        public void setListPhoto(ArrayList<Image> listPhoto) {
            this.listPhoto = listPhoto;
        }

        @Override
        public ContentValues getContentValues() {
            ContentValues values = super.getContentValues();
            values.put(COLUMN_DESC, description);
            return values;
        }

        public boolean isParent() {
            // TODO Auto-generated method stub
            return description.equals(INVALID_DESC);
        }
    }

    public static class TopBaseAudioGuide extends BaseAudioGuide {


        // ===========================================================
        // Constants
        // ===========================================================
        // ===========================================================
        // Fields
        // ===========================================================
        private ArrayList<BaseAudioGuide> childAudioGuides;
        private ArrayList<BaseAudioGuide> filteredAudioGuides;

        // ===========================================================
        // Constructors
        // ===========================================================

        public TopBaseAudioGuide(Cursor c) {
            super(c);

            this.filteredAudioGuides = new ArrayList<BaseAudioGuide>();
        }

        public TopBaseAudioGuide(Museum museum, String language,
                                 boolean isFree, Cursor c) {
            super(museum, language, isFree, c);
            this.filteredAudioGuides = new ArrayList<BaseAudioGuide>();
            // TODO Auto-generated constructor stub
        }

        public TopBaseAudioGuide(BaseAudioGuide audioGuide) {
            super(audioGuide);
            this.filteredAudioGuides = new ArrayList<BaseAudioGuide>();
        }

        // ===========================================================
        // Getter & Setter
        // ===========================================================
        public void setChildAudioGuides(ArrayList<BaseAudioGuide> childAudioGuides) {
            this.childAudioGuides = childAudioGuides;
            this.filteredAudioGuides.clear();
            this.filteredAudioGuides.addAll(childAudioGuides);
        }

        public ArrayList<BaseAudioGuide> getChildAudioGuides() {
            return filteredAudioGuides;
        }

        // ===========================================================
        // Methods for/from SuperClass/Interfaces
        // ===========================================================
        public void filterChildren(CharSequence constraint) {
            if (childAudioGuides == null)
                return;

            constraint = constraint.toString().toLowerCase(AppIsole.getAppLocale());
            if (!TextUtils.isEmpty(constraint)) {
                filteredAudioGuides.clear();
                for (int i = 0, l = childAudioGuides.size(); i < l; i++) {
                    BaseAudioGuide audioGuide = childAudioGuides.get(i);

                    if (audioGuide.toString().toLowerCase(AppIsole.getAppLocale()).contains(constraint)) {
                        filteredAudioGuides.add(audioGuide);
                    }
                }
            } else {
                resetChildren();
            }
        }

        public boolean hasChildren() {
            return !(childAudioGuides == null
                    || childAudioGuides.size() == 0);
        }

        public void resetChildren() {
            if (childAudioGuides == null || filteredAudioGuides == null)
                return;
            this.filteredAudioGuides.clear();
            this.filteredAudioGuides.addAll(childAudioGuides);
        }
        // ===========================================================
        // Methods
        // ===========================================================

    }

    public static class BaseAudioGuide implements Comparable<BaseAudioGuide>, Parcelable {
        // ===========================================================
        // Constants
        // ===========================================================
        // ===========================================================
        // Fields
        // ===========================================================

        private static final String PATTERN = "\\D+";

        private long id;
        private String codeNo;
        private String title;
        private String room;

        protected Museum museum;
        private String languageShort;
        private boolean free;

        private boolean isRandom;

        private static ImageLoader imgLoader = ImageLoader.getInstance();
        private static MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

        protected static final ContentValues values = new ContentValues();

        public static final Parcelable.Creator<BaseAudioGuide> CREATOR
                = new Parcelable.Creator<BaseAudioGuide>() {
            public BaseAudioGuide createFromParcel(Parcel in) {
                return new BaseAudioGuide(in);
            }

            public BaseAudioGuide[] newArray(int size) {
                return new BaseAudioGuide[size];
            }
        };

        @Override
        public int describeContents() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeString(codeNo);
            dest.writeString(title);
            dest.writeString(room);

            dest.writeString(museum.getShortName());
            dest.writeString(languageShort);
            dest.writeInt(free ? 1 : 0);
            dest.writeInt(isRandom ? 1 : 0);

        }

        // ===========================================================
        // Constructors
        // ===========================================================

        private BaseAudioGuide(Parcel in) {
            id = in.readLong();
            codeNo = in.readString();
            title = in.readString();
            room = in.readString();

            museum = Museum.getMuseum(in.readString());
            languageShort = in.readString();
            free = in.readInt() == 1;
            isRandom = in.readInt() == 1;

        }


        public BaseAudioGuide(Cursor c) {
            this.codeNo = c.getString(c.getColumnIndex(COLUMN_CODE_NO));
            this.title = c.getString(c.getColumnIndex(COLUMN_TITLE));
            this.id = c.getLong(c.getColumnIndex(COLUMN_ID));
            this.museum = Museum.getMuseum(c.getString(c.getColumnIndex(COLUMN_MUSEUM)));
            this.languageShort = c.getString(c.getColumnIndex(COLUMN_LANGUAGE));
            this.free = DBUtils.intToBool(c.getInt(c.getColumnIndex(COLUMN_IS_FREE)));
            this.id = c.getLong(c.getColumnIndex(COLUMN_ID));
            this.room = c.getString(c.getColumnIndex(COLUMN_ROOM));

        }


        //////////////////chek this byy bibin


        public BaseAudioGuide(Museum museum, String language, boolean isFree, Cursor c) {
            this.codeNo = c.getString(c.getColumnIndex(COLUMN_CODE_NO));
            this.title = c.getString(c.getColumnIndex(COLUMN_TITLE));
            this.museum = museum;
            this.languageShort = language;
            this.free = isFree;
            this.id = c.getLong(c.getColumnIndex(COLUMN_ID));
            this.room = c.getString(c.getColumnIndex(COLUMN_ROOM));

        }

        public BaseAudioGuide(Museum museum, String language, boolean isFree, Room room, Cursor c) {
            this.codeNo = c.getString(c.getColumnIndex(COLUMN_CODE_NO));
            this.title = c.getString(c.getColumnIndex(COLUMN_TITLE));
            this.museum = museum;
            this.languageShort = language;
            this.free = isFree;
            this.id = c.getLong(c.getColumnIndex(COLUMN_ID));
            this.room = room.getRoomNo();


        }

        public BaseAudioGuide(BaseAudioGuide audioGuide) {
            this.codeNo = audioGuide.codeNo;
            this.title = audioGuide.title;
            this.museum = audioGuide.museum;
            this.languageShort = audioGuide.languageShort;
            this.free = audioGuide.free;
            this.id = audioGuide.id;
            this.room = audioGuide.room;
        }

        public BaseAudioGuide(Museum museum, String language, boolean isFree) {
            this.museum = museum;
            this.languageShort = language;
            this.free = isFree;
        }

        // ===========================================================
        // Getter & Setter
        // ===========================================================


        public long getId() {
            return id;
        }

        public String getCodeNo() {
            return codeNo;
        }

        public Museum getMuseum() {
            return museum;
        }

        public void setRoom(String room) {
            this.room = room;
        }

        public String getRoom() {
            return room;
        }


        public String getParentCode() {
            String parentCode = null;
           /* Scanner scanner = new Scanner(codeNo);
            scanner.useDelimiter(PATTERN);
            if (scanner.hasNext()) {
                parentCode = scanner.next();
            }
            scanner.close();*/
            parentCode = getId(codeNo);
            return parentCode;

        }

        public void setId(String codeNo) {
            this.codeNo = codeNo;
            this.room = getId(codeNo);
           /* Scanner scanner = new Scanner(codeNo);
            scanner.useDelimiter(PATTERN);
            if (scanner.hasNext()) {
                this.room = scanner.next();
                Log.d(TAG, "setId: "+scanner.next());
            }
            scanner.close();*/
        }


        //Set room number in audio guide (new pattern ) bibin babu
        private static String getId(String codeNo) {
            int codeLength = codeNo.length();
            int roomCode = 0;
            StringBuilder dataRoomCode = new StringBuilder();
            boolean breakPoint = false;
            for (int i = 0; i < codeLength; i++) {
                char item = codeNo.charAt(i);
                if (Character.isDigit(item)) {
                    roomCode = Integer.parseInt(String.valueOf(item));
                    if (breakPoint) {
                        dataRoomCode.append(roomCode);
                        continue;
                    }
                    if (roomCode > 0) {
                        dataRoomCode.append(roomCode);
                        breakPoint = true;
                    }
                } else {
                    if (Character.isUpperCase(item)) {
                        dataRoomCode.append(item);
                    }
                    break;
                }
            }
            if (dataRoomCode.toString().isEmpty()) {
                dataRoomCode.append(roomCode);
            }
            return dataRoomCode.toString();
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLanguageShort() {
            return languageShort;
        }

        public String getAudioFolder(Context context) {
            return AppIsole.getAudioFolder(context, museum, languageShort, free);
        }

        public String getAudioGalleryFolder(Context context) {
            if (!free)
                return AppIsole.getPaidAudioGalleryFolder(context, museum);
            else
                return AppIsole.getFreeAudioGalleryFolder(context, museum);
        }

        public String getCoverArtPath(Context context) {
            return getAudioGalleryFolder(context) + File.separator + codeNo + "-01.jpg";

        }

        public String getAudioPath(Context context) {
            Log.d("AudioPath", getAudioFolder(context) + File.separator + codeNo + ".mp3");
            return getAudioFolder(context) + File.separator + codeNo + ".mp3";
        }

        public long getAudioDuration(Context context) {
            metaRetriever.setDataSource(getAudioPath(context));
            String duration = metaRetriever.extractMetadata(MediaMetadataRetriever
                    .METADATA_KEY_DURATION);
            return Long.parseLong(duration);
        }

        public ContentValues getContentValues() {
            values.clear();

            values.put(COLUMN_CODE_NO, codeNo);
            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_MUSEUM, museum.getShortName());
            values.put(COLUMN_LANGUAGE, languageShort);
            values.put(COLUMN_IS_FREE, DBUtils.boolToInt(free));
            values.put(COLUMN_ROOM, room);

            return values;
        }

        // ===========================================================
        // Methods for/from SuperClass/Interfaces
        // ===========================================================

        @Override
        public String toString() {
            return codeNo + " " + title;
        }

        @Override
        public int compareTo(BaseAudioGuide another) {
            // TODO Auto-generated method stub
            return this.codeNo.compareTo(another.codeNo);
        }

        // ===========================================================
        // Methods
        // ===========================================================

        /**
         * Get the id of the given song.
         *
         * @param song The Song to get the id from.
         * @return The id, or 0 if the given song is null.
         */
        public static long getId(AudioGuide song) {
            if (song == null)
                return 0;
            return song.getId();
        }

        public boolean isRandom() {
            return isRandom;
        }

        public Bitmap getCover(Context context, int width, int height) {

            return imgLoader.loadImageSync("file://" + getCoverArtPath(context), new ImageSize(width, height));
        }

        public boolean isFree() {
            return free;
        }


    }


}


