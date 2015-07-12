package perez.marcos.com.newz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
        public AlarmBroadcastReceiver() {
        }
        @Override
        public void onReceive(Context context, Intent intent) {
                Intent i = new Intent();
                i.setClassName("perez.marcos.com.newz", "perez.marcos.com.newz.MainActivity");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
        }
}
