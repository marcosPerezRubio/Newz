package perez.marcos.com.newz.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import perez.marcos.com.newz.MainActivity;
import perez.marcos.com.newz.R;
import perez.marcos.com.newz.Utility;
import perez.marcos.com.newz.data.NewzContract;

/**
 * Created by marcos on 09/06/2015.
 */
public class NewzSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = NewzSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    //This would work on a further version i would like to implement with more time.
    //public static final int SYNC_TIME_MILLIS = 58 * 180*1000;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int NEWZ_NOTIFICATION_ID = 3004;
    final static String GROUP_KEY_NOT = "group_key_not";

    private static final String[] NOTIFY_NEWZ_PROJECTION = new String[] {

            NewzContract.SharedEntry.TITLE,
            NewzContract.SharedEntry.THUMB_URL,
            NewzContract.SharedEntry.POST_URL,
            NewzContract.SharedEntry.ABSTRACT,
            NewzContract.SharedEntry._ID
    };

    // these indices must match the projection
    private static final int INDEX_TITLE = 0;
    private static final int INDEX_THUMB_URL= 1;
    private static final int INDEX_POST_URL = 2;
    private static final int INDEX_ABSTRACT = 3;
    private static final int INDEX_ID = 4;


    public NewzSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        //Devuelve la preferencia de los art�culos: viewed, shared, emailed
        String articleType = Utility.getNewzType(getContext());

        //Devuelve los dias.
        String articleDays = Utility.getDays(getContext());


        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String newzJsonStr = null;

        try {
            // Construct the URL for the NYTIME Query

            final String NEWZ_BASE_URL = "http://api.nytimes.com/svc/mostpopular/v2/";
            final String SECTION = "/all-sections/";
            final String apikey = "ce962bc2136549163faaec268f00ec54:7:72252416";
            String format = ".json";
            String API_ROUTE_PARAM = "?api-key=";
            String final_ = NEWZ_BASE_URL + articleType + SECTION + articleDays + format + API_ROUTE_PARAM + apikey;
            Uri builtUri = Uri.parse(final_);
            URL url = new URL(builtUri.toString());

            // Create the request to nyTimes, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            newzJsonStr = buffer.toString();
            getNewzDataFromJson(newzJsonStr, articleType);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getNewzDataFromJson(String NewzJsonStr, String articleType) throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        final String TITLE = "title";
        final String AUTHOR = "byline";
        final String ABSTRACT = "abstract";
        final String DATE = "published_date";
        final String URL = "url";
        final String MEDIA = "media";
        final String SECTION = "section";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String LIST = "results";

        try {
            JSONObject forecastJson = new JSONObject(NewzJsonStr);
            JSONArray newzArray = forecastJson.getJSONArray(LIST);

            //We store the insertion date
            long date_insert = System.currentTimeMillis();

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(newzArray.length());

            for(int i = 0; i < newzArray.length(); i++) {
                // These are the values that will be collected.
                String author;
                String date;
                String abs; //abstract
                String post_url;
                String thumb_url;
                String sect;
                String title;

                //countType especifica por que canal se ha compartido m�s la noticia

                // Get the JSON object representing the new
                JSONObject newz = newzArray.getJSONObject(i);

                title =  newz.getString(TITLE);
                author = newz.getString(AUTHOR);
                date = newz.getString(DATE);
                abs = newz.getString(ABSTRACT);
                post_url= newz.getString(URL);
                sect = newz.getString(SECTION);


                //Cogemos el elemento media, accedemos al vector media-data -> su vector para cada resolucion --> cogemos la url

                //No se puede coger como object
                JSONArray mediaArray = new JSONArray();
                if(newz.getString(MEDIA).length() > 0) {
                    mediaArray = newz.getJSONArray(MEDIA);
                    int y =mediaArray.getJSONObject(0).getJSONArray("media-metadata").length();
                    thumb_url = mediaArray.getJSONObject(0).getJSONArray("media-metadata").getJSONObject(y-1).getString("url");
                }
                else {
                    thumb_url = "";
                }


                ContentValues newzValues = new ContentValues();

                // Since this data is also sent in-order and the first day is always the
                // current day, we're going to take advantage of that to get a nice
                // normalized UTC date for all of our weather.


                if (articleType.equals(NewzContract.PATH_EMAILED)){
                    newzValues.put(NewzContract.EmailedEntry.ABSTRACT, abs);
                    newzValues.put(NewzContract.EmailedEntry.TITLE, title);
                    newzValues.put(NewzContract.EmailedEntry.AUTHOR, author);
                    newzValues.put(NewzContract.EmailedEntry.DATE, date);
                    newzValues.put(NewzContract.EmailedEntry.POST_URL, post_url);
                    newzValues.put(NewzContract.EmailedEntry.SECTION, sect);
                    newzValues.put(NewzContract.EmailedEntry.THUMB_URL, thumb_url);
                    newzValues.put(NewzContract.EmailedEntry.DATE_INSERTED, date_insert);
                }
                else if (articleType.equals(NewzContract.PATH_SHARED)) {
                    newzValues.put(NewzContract.SharedEntry.TITLE, title);
                    newzValues.put(NewzContract.SharedEntry.ABSTRACT, abs);
                    newzValues.put(NewzContract.SharedEntry.AUTHOR, author);
                    newzValues.put(NewzContract.SharedEntry.DATE, date);
                    newzValues.put(NewzContract.SharedEntry.POST_URL, post_url);
                    newzValues.put(NewzContract.SharedEntry.SECTION, sect);
                    newzValues.put(NewzContract.SharedEntry.THUMB_URL, thumb_url);
                    newzValues.put(NewzContract.SharedEntry.DATE_INSERTED, date_insert);
                }
                else if(articleType.equals(NewzContract.PATH_VIEWED)){
                    newzValues.put(NewzContract.ViewedEntry.TITLE, title);
                    newzValues.put(NewzContract.ViewedEntry.ABSTRACT, abs);
                    newzValues.put(NewzContract.ViewedEntry.AUTHOR, author);
                    newzValues.put(NewzContract.ViewedEntry.DATE, date);
                    newzValues.put(NewzContract.ViewedEntry.POST_URL, post_url);
                    newzValues.put(NewzContract.ViewedEntry.SECTION, sect);
                    newzValues.put(NewzContract.ViewedEntry.THUMB_URL, thumb_url);
                    newzValues.put(NewzContract.ViewedEntry.DATE_INSERTED, date_insert);
                }
                else{
                    Log.v("ARTICLE","Unknow articletype: " + articleType);
                }

                cVVector.add(newzValues);
            }

            //long lastSync = date_insert - SYNC_TIME_MILLIS;
            int inserted = 0;
            // add to database
            if ( cVVector.size() > 0 ) {

                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                switch (articleType){
                    case NewzContract.PATH_EMAILED:
                        //At this point i delete all the data on the table, on further versions i would like to implement
                        //the delete with the inserted date on the database
                        getContext().getContentResolver().delete(
                                NewzContract.EmailedEntry.CONTENT_URI,
                                NewzContract.EmailedEntry._ID + " <= ?",
                                new String[]{"50"});
                        getContext().getContentResolver().bulkInsert(NewzContract.EmailedEntry.CONTENT_URI, cvArray);

                        break;
                    case NewzContract.PATH_SHARED:
                        getContext().getContentResolver().delete(
                                NewzContract.SharedEntry.CONTENT_URI,
                                NewzContract.SharedEntry._ID + " <= ?",
                                new String[]{"50"});
                        getContext().getContentResolver().bulkInsert(NewzContract.SharedEntry.CONTENT_URI, cvArray);
                        break;
                    case NewzContract.PATH_VIEWED:
                        getContext().getContentResolver().delete(
                                NewzContract.ViewedEntry.CONTENT_URI,
                                NewzContract.ViewedEntry._ID + " <= ?",
                                new String[]{"50"});
                        getContext().getContentResolver().bulkInsert(NewzContract.ViewedEntry.CONTENT_URI, cvArray);
                        break;
                    default:
                        Log.v("ARTICLE", "unknown article: " + articleType);
                }
                notifyNewz();
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void notifyNewz() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if ( displayNotifications ) {

            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                // Last sync was more than a day ago, let's send a notification with the last most shared, most emailed or most viewed
                // depending on preferences
                String preferedArticle = Utility.getNewzType(context);

                Uri newzUri = null;
                if (preferedArticle.equals(NewzContract.PATH_EMAILED)){
                    newzUri =NewzContract.EmailedEntry.buildNewzUri();
                }
                else if (preferedArticle.equals(NewzContract.PATH_VIEWED)){
                    newzUri = NewzContract.ViewedEntry.buildNewzUri();
                }
                else if (preferedArticle.equals(NewzContract.PATH_SHARED)){
                    newzUri = NewzContract.SharedEntry.buildNewzUri();
                }
                else {
                    Log.v("sync","Unknown prefered:" + preferedArticle);
                }

                // we'll query our contentProvider, as always
                Cursor cursor = context.getContentResolver().query(newzUri, NOTIFY_NEWZ_PROJECTION, null, null, null);

                if (cursor.moveToFirst()) {
                    String abs = cursor.getString(INDEX_ABSTRACT);
                    String title = cursor.getString(INDEX_TITLE);
                    String post_url = cursor.getString(INDEX_POST_URL);
                    String thumb_url = cursor.getString(INDEX_THUMB_URL);
                    String id = cursor.getString(INDEX_ID);

                    //Preparamos los parámetros
                    String apptitle = context.getString(R.string.app_name);

                    Resources resources = context.getResources();
                    Bitmap icon= null;
                    int iconId = -1;
                    try {
                        icon = Picasso.with(context).load(thumb_url).get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        if (icon == null) {
                            iconId = R.drawable.icon;
                            try {
                                icon = Picasso.with(context).load(R.drawable.icon).get();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    Intent intentApp = new Intent(context, MainActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(intentApp);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );

                    iconId = R.drawable.icon;

                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
                    // notifications.  Just throw in some data.
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setColor(resources.getColor(R.color.button_material_light))
                                    .setSmallIcon(iconId)
                                    .setContentIntent(resultPendingIntent)
                                    .setContentTitle(apptitle)
                                    .setSubText(abs)
                                    .setGroup(GROUP_KEY_NOT)
                                    .setContentText(title);

                    // Now create the Big picture notification.
                    NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle(mBuilder);
                    bigPictureStyle.bigPicture(icon);
                    Notification notification = bigPictureStyle.build();
                    // Put the auto cancel notification flag
                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    // NEWZ_NOTIFICATION_ID allows you to update the notification later on.
                    mNotificationManager.notify(NEWZ_NOTIFICATION_ID, notification);

                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.commit();
                }
                cursor.close();
            }
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        NewzSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
