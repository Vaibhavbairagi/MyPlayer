package com.vaibhav.myplayer.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.vaibhav.myplayer.Songs;

import java.util.ArrayList;

public class EchoDatabase extends SQLiteOpenHelper {
    public static String DB_NAME = "FavouriteDatabase", TABLE_NAME = "FavouriteTable", COLUMN_ID = "SongID", COLUMN_SONG_TITLE = "SongTitle", COLUMN_SONG_ARTIST = "SongArtist", COLUMN_SONG_PATH = "SongPath";
    public static int DB_VERSION = 1;
    ArrayList<Songs> _songList = new ArrayList<>();

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_SONG_ARTIST + " TEXT," + COLUMN_SONG_TITLE + " TEXT," + COLUMN_SONG_PATH + " TEXT" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, int i, int i1) {

    }

    public void storeAsFavourite(int id, String artist, String songTitle, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, id);
        contentValues.put(COLUMN_SONG_ARTIST, artist);
        contentValues.put(COLUMN_SONG_TITLE, songTitle);
        contentValues.put(COLUMN_SONG_PATH, path);
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }

    public ArrayList<Songs> queryDBList() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query_params = "SELECT * FROM " + TABLE_NAME;
            Cursor cSor = db.rawQuery(query_params, null);
            if (cSor.moveToFirst()) {
                do {
                    int _id = cSor.getInt(cSor.getColumnIndexOrThrow(COLUMN_ID));
                    String _artist = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_ARTIST));
                    String _title = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_TITLE));
                    String _songPath = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_PATH));
                    _songList.add(new Songs((long) _id, _title, _artist, _songPath, (long) 0));
                } while (cSor.moveToNext());
            } else {
                return null;
            }
            cSor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return _songList;
    }

    public boolean checkIfIdExists(long _id) {
        int storeId = -1090;
        SQLiteDatabase db = this.getReadableDatabase();
        String query_params = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = " + (int) _id;
        Cursor cSor = db.rawQuery(query_params, null);
        if (cSor.moveToFirst()) {
            do {
                storeId = cSor.getColumnIndexOrThrow(COLUMN_ID);
            } while (cSor.moveToNext());
        } else {
            return false;
        }
        cSor.close();
        return storeId != -1090;
    }

    public void deleteFavourite(int _id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=" + _id, null);
        db.close();
    }

    public int checkSize() {
        int counter = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query_params = "SELECT * FROM " + TABLE_NAME;
        Cursor cSor = db.rawQuery(query_params, null);
        if (cSor.moveToFirst()) {
            do {
                counter = counter + 1;
            } while (cSor.moveToNext());
        } else {
            return 0;
        }
        cSor.close();
        return counter;
    }

    public EchoDatabase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public EchoDatabase(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
}
