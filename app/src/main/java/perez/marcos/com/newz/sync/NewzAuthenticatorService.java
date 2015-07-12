package perez.marcos.com.newz.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by marcos on 09/06/2015.
 */
public class NewzAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private NewzAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new NewzAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
