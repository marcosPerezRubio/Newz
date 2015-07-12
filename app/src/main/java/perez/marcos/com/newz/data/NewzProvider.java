package perez.marcos.com.newz.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by marcos on 09/06/2015.
 */
public class NewzProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private NewzDbHelper mOpenHelper;

    static final int SHARED = 100;
    static final int SHARED_WITH_ID = 101;
    static final int EMAILED = 200;
    static final int EMAILED_WITH_ID = 201;
    static final int VIEWED = 300;
    static final int VIEWED_WITH_ID = 301;


    private static final String querySharedID =
            NewzContract.SharedEntry.TABLE_NAME +
                    "." + NewzContract.SharedEntry._ID+ " = ? ";
    private static final String queryEmailedID =
            NewzContract.EmailedEntry.TABLE_NAME +
                    "." + NewzContract.EmailedEntry._ID+ " = ? ";
    private static final String queryViewedID =
            NewzContract.ViewedEntry.TABLE_NAME +
                    "." + NewzContract.ViewedEntry._ID+ " = ? ";

    @Override
    public boolean onCreate() {
        mOpenHelper = new NewzDbHelper(getContext());
        return true;
    }


    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = NewzContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, NewzContract.PATH_SHARED, SHARED);
        matcher.addURI(authority, NewzContract.PATH_EMAILED, EMAILED);
        matcher.addURI(authority, NewzContract.PATH_VIEWED, VIEWED);
        matcher.addURI(authority, NewzContract.PATH_SHARED + "/#", SHARED_WITH_ID);
        matcher.addURI(authority, NewzContract.PATH_EMAILED + "/#", EMAILED_WITH_ID);
        matcher.addURI(authority, NewzContract.PATH_VIEWED + "/#", VIEWED_WITH_ID);

        return matcher;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHARED:
                return NewzContract.SharedEntry.CONTENT_TYPE;
            case EMAILED:
                return NewzContract.EmailedEntry.CONTENT_TYPE;
            case VIEWED:
                return NewzContract.ViewedEntry.CONTENT_TYPE;
            case SHARED_WITH_ID:
                return NewzContract.SharedEntry.CONTENT_ITEM_TYPE;
            case EMAILED_WITH_ID:
                return NewzContract.EmailedEntry.CONTENT_ITEM_TYPE;
            case VIEWED_WITH_ID:
                return NewzContract.ViewedEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }




    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
// Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        Long id;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case SHARED: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewzContract.SharedEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case EMAILED: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewzContract.EmailedEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case VIEWED: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewzContract.ViewedEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case SHARED_WITH_ID:
                id = NewzContract.getIdFromUri(uri);
                return mOpenHelper.getReadableDatabase().query(
                        NewzContract.SharedEntry.TABLE_NAME,
                        projection,
                        querySharedID,
                        new String[]{Long.toString(id)},
                        null,
                        null,
                        sortOrder
                );

            case EMAILED_WITH_ID:
                id = NewzContract.getIdFromUri(uri);
                return mOpenHelper.getReadableDatabase().query(
                        NewzContract.EmailedEntry.TABLE_NAME,
                        projection,
                        queryEmailedID,
                        new String[]{Long.toString(id)},
                        null,
                        null,
                        sortOrder
                );
            case VIEWED_WITH_ID:
                id = NewzContract.getIdFromUri(uri);
                return mOpenHelper.getReadableDatabase().query(
                        NewzContract.ViewedEntry.TABLE_NAME,
                        projection,
                        queryViewedID,
                        new String[]{Long.toString(id)},
                        null,
                        null,
                        sortOrder
                );
            default:
                throw new UnsupportedOperationException("Unknown uri query: " + uri + " and urimatch " + match);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case EMAILED:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(NewzContract.EmailedEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case VIEWED:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(NewzContract.ViewedEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case SHARED:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(NewzContract.SharedEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }





    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case SHARED: {
                long _id = db.insertWithOnConflict(NewzContract.SharedEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    returnUri = NewzContract.SharedEntry.buildSharedUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case EMAILED: {
                long _id = db.insertWithOnConflict(NewzContract.EmailedEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    returnUri = NewzContract.EmailedEntry.buildEmailedUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case VIEWED: {
                long _id = db.insertWithOnConflict(NewzContract.ViewedEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    returnUri = NewzContract.ViewedEntry.buildViewedUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case SHARED:
                rowsDeleted = db.delete(
                        NewzContract.SharedEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case VIEWED:
                rowsDeleted = db.delete(
                        NewzContract.ViewedEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case EMAILED:
                rowsDeleted = db.delete(
                        NewzContract.EmailedEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case SHARED:
                rowsUpdated = db.updateWithOnConflict(NewzContract.SharedEntry.TABLE_NAME, values, selection,
                        selectionArgs, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case VIEWED:
                rowsUpdated = db.updateWithOnConflict(NewzContract.ViewedEntry.TABLE_NAME, values, selection,
                        selectionArgs, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case EMAILED:
                rowsUpdated = db.updateWithOnConflict(NewzContract.EmailedEntry.TABLE_NAME, values, selection,
                        selectionArgs, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;    }




    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

}