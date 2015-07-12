package perez.marcos.com.newz.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by marcos on 09/06/2015.
 */
public class NewzSyncService extends Service {
    private static final Object mSyncAdapterLock = new Object();
    private static NewzSyncAdapter mAdapter = null;

    @Override
    public void onCreate() {
        synchronized (mSyncAdapterLock) {
            if (mAdapter == null) {
                mAdapter = new NewzSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAdapter.getSyncAdapterBinder();
    }

}
