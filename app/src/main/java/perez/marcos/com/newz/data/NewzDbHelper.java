package perez.marcos.com.newz.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import perez.marcos.com.newz.data.NewzContract.EmailedEntry;
import perez.marcos.com.newz.data.NewzContract.SharedEntry;
import perez.marcos.com.newz.data.NewzContract.ViewedEntry;

/**
 * Created by marcos on 09/06/2015.
 */
public class NewzDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 5;
    static final String DATABASE_NAME = "newz.db";

    public NewzDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_VIEWED_TABLE =
                "CREATE TABLE " + ViewedEntry.TABLE_NAME + " (" +
                ViewedEntry._ID + " INTEGER PRIMARY KEY," +
                ViewedEntry.TITLE + " TEXT NOT NULL, " +
                ViewedEntry.THUMB_URL + " TEXT NOT NULL, " +
                ViewedEntry.ABSTRACT + " TEXT NOT NULL, " +
                ViewedEntry.SECTION + " TEXT NOT NULL, " +
                ViewedEntry.AUTHOR + " TEXT NOT NULL, " +
                ViewedEntry.POST_URL + " TEXT NOT NULL UNIQUE, " +
                ViewedEntry.DATE + " TEXT NOT NULL, " +
                ViewedEntry.DATE_INSERTED + " INTEGER NOT NULL " +
                " );";

        final String SQL_CREATE_SHARED_TABLE = "CREATE TABLE " + SharedEntry.TABLE_NAME + " (" +
                SharedEntry._ID + " INTEGER PRIMARY KEY," +
                SharedEntry.TITLE + " TEXT NOT NULL, " +
                SharedEntry.THUMB_URL + " TEXT NOT NULL, " +
                SharedEntry.ABSTRACT + " TEXT NOT NULL, " +
                SharedEntry.SECTION + " TEXT NOT NULL, " +
                SharedEntry.AUTHOR + " TEXT NOT NULL, " +
                SharedEntry.POST_URL + " TEXT NOT NULL UNIQUE, " +
                SharedEntry.DATE + " TEXT NOT NULL, " +
                SharedEntry.DATE_INSERTED + " INTEGER NOT NULL " +
                " );";

        final String SQL_CREATE_EMAILED_TABLE ="CREATE TABLE " + EmailedEntry.TABLE_NAME + " (" +
                EmailedEntry._ID + " INTEGER PRIMARY KEY," +
                EmailedEntry.TITLE + " TEXT NOT NULL, " +
                EmailedEntry.THUMB_URL + " TEXT NOT NULL, " +
                EmailedEntry.ABSTRACT + " TEXT NOT NULL, " +
                EmailedEntry.SECTION + " TEXT NOT NULL, " +
                EmailedEntry.AUTHOR + " TEXT NOT NULL, " +
                EmailedEntry.POST_URL + " TEXT NOT NULL UNIQUE, " +
                EmailedEntry.DATE + " TEXT NOT NULL, " +
                EmailedEntry.DATE_INSERTED + " INTEGER NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_VIEWED_TABLE);
        db.execSQL(SQL_CREATE_SHARED_TABLE);
        db.execSQL(SQL_CREATE_EMAILED_TABLE);
        }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ViewedEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SharedEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EmailedEntry.TABLE_NAME);
        onCreate(db);
    }
}
