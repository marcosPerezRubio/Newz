package perez.marcos.com.newz;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by marcos on 10/06/2015.
 */
public class Utility {


    public static String getDays(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_days_key),
                context.getString(R.string.pref_days_default));    }

    

    public static String getNewzType(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_newz_key),
                context.getString(R.string.pref_newz_default));

    }

}
