package perez.marcos.com.newz;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements NewzFragment.Callback{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;
    private String mNewzType;
    private String mDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNewzType = Utility.getNewzType(this);


        setContentView(R.layout.activity_main);
        if (findViewById(R.id.newz_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.newz_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            Log.v(LOG_TAG,"mTwoPane FALSE");
            getSupportActionBar().setElevation(0f);
        }
        NewzFragment newzfragment = ((NewzFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_newz));
        newzfragment.setUseFirstLayout(!mTwoPane);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent i = new Intent(this, SettingsActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String newztype = Utility.getNewzType(this);
        String days= Utility.getDays(this);
        if (newztype != null && !newztype.equals(mNewzType) || days != null && !days.equals(mDays)) {
            NewzFragment nf = (NewzFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_newz);
            if ( null != nf ) nf.onTypeOrDaysChanged();
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) df.onTypeChanged(newztype);
            mNewzType = newztype;
            mDays = days;
        }
        ActionBar a = getSupportActionBar();
        if (a != null) {
            if(mNewzType.equals(getResources().getString(R.string.pref_newz_value_shared))){
                a.setTitle(R.string.pref_newz_label_shared);
            }
            else if(mNewzType.equals(getResources().getString(R.string.pref_newz_value_emailed))){
                a.setTitle(R.string.pref_newz_label_emailed);
            }
            else {
                a.setTitle(R.string.pref_newz_label_viewed);
            }
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.newz_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
