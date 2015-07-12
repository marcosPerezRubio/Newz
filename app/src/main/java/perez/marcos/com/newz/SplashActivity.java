package perez.marcos.com.newz;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;

import perez.marcos.com.newz.sync.NewzSyncAdapter;


public class SplashActivity extends Activity {

    private SharedPreferences prefs;
    private ImageView gif;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        prefs = getSharedPreferences("NEWZ", MODE_PRIVATE);
        boolean first = prefs.getBoolean("FirstTime", true);
        NewzSyncAdapter.initializeSyncAdapter(this);
        if (first){
            SharedPreferences.Editor edito = prefs.edit();
            edito.putBoolean("FirstTime", false);
            edito.commit();
            gif = (ImageView) findViewById(R.id.imageView2);
            Ion.with(gif).load("android.resource://perez.marcos.com.newz/" + R.drawable.loading);

            Intent alarmIntent = new Intent(this, AlarmBroadcastReceiver.class);
            //Wrap in a pending intent which only fires once.
            PendingIntent pi = PendingIntent.getBroadcast(this, 0,alarmIntent,0);//getBroadcast(context, 0, i, 0);
            AlarmManager am= (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
        }
        else {
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
