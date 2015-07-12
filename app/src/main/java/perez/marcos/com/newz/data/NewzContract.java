package perez.marcos.com.newz.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by marcos on 09/06/2015.
 */
public class NewzContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "perez.marcos.com.newz";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.

    public static final String PATH_VIEWED = "mostviewed";
    public static final String PATH_SHARED = "mostshared";
    public static final String PATH_EMAILED = "mostemailed";



    /* Inner class that defines the table contents of the location table */
    public static final class ViewedEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIEWED).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIEWED;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIEWED;


        // Table name
        public static final String TABLE_NAME = "viewed";
        public static final String TITLE = "title";
        public static final String THUMB_URL = "thumb_url";
        public static final String ABSTRACT = "abstract";
        public static final String SECTION = "section";
        public static final String AUTHOR = "author";
        public static final String DATE = "date";
        public static final String POST_URL = "post_url";
        public static final String DATE_INSERTED = "date_inserted";

        public static Uri buildViewedUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildNewzUri() {
            return CONTENT_URI.buildUpon().build();
        }

    }

    /* Inner class that defines the table contents of the weather table */
    public static final class SharedEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHARED).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHARED;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHARED;

        // Table name
        public static final String TABLE_NAME = "shared";
        public static final String TITLE = "title";
        public static final String THUMB_URL = "thumb_url";
        public static final String ABSTRACT = "abstract";
        public static final String SECTION = "section";
        public static final String AUTHOR = "author";
        public static final String DATE = "date";
        public static final String POST_URL = "post_url";
        public static final String DATE_INSERTED = "date_inserted";

        public static Uri buildSharedUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildNewzUri() {
            return CONTENT_URI.buildUpon().build();
        }


    }

    /* Inner class that defines the table contents of the weather table */
    public static final class EmailedEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EMAILED).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EMAILED;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EMAILED;

        // Table name
        public static final String TABLE_NAME = "emailed";
        public static final String TITLE = "title";
        public static final String THUMB_URL = "thumb_url";
        public static final String ABSTRACT = "abstract";
        public static final String SECTION = "section";
        public static final String AUTHOR = "author";
        public static final String DATE = "date";
        public static final String POST_URL = "post_url";
        public static final String DATE_INSERTED = "date_inserted";

        public static Uri buildEmailedUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildNewzUri() {
            return CONTENT_URI.buildUpon().build();
        }

    }

    public static long getIdFromUri(Uri uri) {
        return Long.parseLong(uri.getPathSegments().get(1));
    }


}
