/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   TVProgramProvider.java
 *  @brief  providing content to this application.
 *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
 *
 *  ALL Rights Reserved, Copyright(C) ROCKCHIP LIMITED 2011
 */
/********************************************************************************************************************/
package com.rockchip.tvbox.provider;

import com.rockchip.tvbox.provider.TVProgram.Programs;
import com.rockchip.tvbox.utils.Logger;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class TVProgramProvider extends ContentProvider {

    private static final String TAG = "TVProgramProvider";

    private static final String DATABASE_NAME = "rk_tvbox_programs.db";
    private static final int DATABASE_VERSION = 2;
    private static final String PROGRAM_TABLE_NAME = "programs";

    private static HashMap<String, String> sProgramsProjectionMap;
    private static HashMap<String, String> sLiveFolderProjectionMap;

    private static final int PROGRAMS = 1;
    private static final int PROGRAM_ID = 2;
    private static final int LIVE_FOLDER_PROGRAMS = 3;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + PROGRAM_TABLE_NAME + " ("
                    + Programs._ID + " INTEGER PRIMARY KEY,"
                    + Programs.SERVICEID + " INTEGER,"
                    + Programs.SERVICENAME + " TEXT,"
                    + Programs.FREQ + " INTEGER,"
                    + Programs.BW + " INTEGER,"
                    + Programs.TYPE + " INTEGER,"
                    + Programs.FAV + " INTEGER,"
                    + Programs.ENCRYPT + " INTEGER,"
                    + Programs.LCN + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS programs");
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
    	Logger.d("Query:"+sUriMatcher.match(uri));
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(PROGRAM_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
        case PROGRAMS:
            qb.setProjectionMap(sProgramsProjectionMap);
            break;

        case PROGRAM_ID:
            qb.setProjectionMap(sProgramsProjectionMap);
            qb.appendWhere(Programs._ID + "=" + uri.getPathSegments().get(1));
            break;

        case LIVE_FOLDER_PROGRAMS:
            qb.setProjectionMap(sLiveFolderProjectionMap);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = TVProgram.Programs.SORT_ORDER_BY_ID;
        } else {
            orderBy = sortOrder;
        }
        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        
        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case PROGRAMS:
        case LIVE_FOLDER_PROGRAMS:
            return Programs.CONTENT_TYPE;

        case PROGRAM_ID:
            return Programs.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != PROGRAMS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

//        Long now = Long.valueOf(System.currentTimeMillis());
        
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss");
//    	Date curDate = new Date(System.currentTimeMillis());//获取当前时间      
//    	String now = formatter.format(curDate); 
        if (values.containsKey(TVProgram.Programs.SERVICEID) == false) {
            values.put(TVProgram.Programs.SERVICEID, 0);
        }
        
        if (values.containsKey(TVProgram.Programs.SERVICENAME) == false) {
            Resources r = Resources.getSystem();
            values.put(TVProgram.Programs.SERVICENAME, r.getString(android.R.string.untitled));
        }

        if (values.containsKey(TVProgram.Programs.FREQ) == false) {
            values.put(TVProgram.Programs.FREQ, 0);
        }
        
        if (values.containsKey(TVProgram.Programs.BW) == false) {
            values.put(TVProgram.Programs.BW, 0);
        }
        
        if (values.containsKey(TVProgram.Programs.TYPE) == false) {
            values.put(TVProgram.Programs.TYPE, 0);
        }
        
        if (values.containsKey(TVProgram.Programs.FAV) == false) {
            values.put(TVProgram.Programs.FAV, 0);
        }
        
        if (values.containsKey(TVProgram.Programs.ENCRYPT) == false) {
            values.put(TVProgram.Programs.ENCRYPT, 0);
        }
        
        if (values.containsKey(TVProgram.Programs.LCN) == false) {
            values.put(TVProgram.Programs.LCN, 0);
        }
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(PROGRAM_TABLE_NAME, Programs.FREQ, values);
        if (rowId > 0) {
            Uri programUri = ContentUris.withAppendedId(TVProgram.Programs.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(programUri, null);
            return programUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        Logger.d("delete uri:"+sUriMatcher.match(uri));
        switch (sUriMatcher.match(uri)) {
        case PROGRAMS:
            count = db.delete(PROGRAM_TABLE_NAME, where, whereArgs);
            break;

        case PROGRAM_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(PROGRAM_TABLE_NAME, Programs._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
    	Logger.d("update db");
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case PROGRAMS:
            count = db.update(PROGRAM_TABLE_NAME, values, where, whereArgs);
            break;

        case PROGRAM_ID:
            String serviceId = uri.getPathSegments().get(1);
            count = db.update(PROGRAM_TABLE_NAME, values, Programs._ID + "=" + serviceId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(TVProgram.AUTHORITY, "programs", PROGRAMS);
        sUriMatcher.addURI(TVProgram.AUTHORITY, "programs/#", PROGRAM_ID);
        sUriMatcher.addURI(TVProgram.AUTHORITY, "live_folders/programs", LIVE_FOLDER_PROGRAMS);

        sProgramsProjectionMap = new HashMap<String, String>();
        sProgramsProjectionMap.put(Programs._ID, Programs._ID);
        sProgramsProjectionMap.put(Programs.SERVICEID, Programs.SERVICEID);
        sProgramsProjectionMap.put(Programs.SERVICENAME, Programs.SERVICENAME);
        sProgramsProjectionMap.put(Programs.FREQ, Programs.FREQ);
        sProgramsProjectionMap.put(Programs.BW, Programs.BW);
        sProgramsProjectionMap.put(Programs.TYPE, Programs.TYPE);
        sProgramsProjectionMap.put(Programs.FAV, Programs.FAV);
        sProgramsProjectionMap.put(Programs.ENCRYPT, Programs.ENCRYPT);
        sProgramsProjectionMap.put(Programs.LCN, Programs.LCN);
        
        // Support for Live Folders.
        sLiveFolderProjectionMap = new HashMap<String, String>();
        sLiveFolderProjectionMap.put(LiveFolders._ID, Programs._ID + " AS " +
                LiveFolders._ID);
        sLiveFolderProjectionMap.put(LiveFolders.NAME, Programs.SERVICENAME + " AS " +
                LiveFolders.NAME);
        // Add more columns here for more robust Live Folders.
    }
}
