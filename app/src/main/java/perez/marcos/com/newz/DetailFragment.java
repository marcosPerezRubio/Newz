package perez.marcos.com.newz;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import perez.marcos.com.newz.data.NewzContract;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    private static final int DETAIL_LOADER = 0;

    private String mType;
    private Uri mUri;
    private String mNewz;

    private ShareActionProvider mShareActionProvider;


    private ImageView mImage;
    private TextView mTitle;
    private TextView mAuthor;
    private TextView mDate;
    private TextView mSection;
    private TextView mAbstract;
    private Button mButton;

    private static final int COL_NEWZ_ID = 0;
    private static final int COL_ABSTRACT = 1;
    private static final int COL_SECTION = 2;
    private static final int COL_POST_URL = 3;
    private static final int COL_AUTHOR = 4;
    private static final int COL_DATE = 5;
    private static final int COL_THUMB_URL = 6;
    private static final int COL_TITLE = 7;


    private static final String[] DETAIL_COLUMNS_EMAILED = {
            NewzContract.EmailedEntry.TABLE_NAME + "." + NewzContract.EmailedEntry._ID,
            NewzContract.EmailedEntry.ABSTRACT,
            NewzContract.EmailedEntry.SECTION,
            NewzContract.EmailedEntry.POST_URL,
            NewzContract.EmailedEntry.AUTHOR,
            NewzContract.EmailedEntry.DATE,
            NewzContract.EmailedEntry.THUMB_URL,
            NewzContract.EmailedEntry.TITLE,
    };

    private static final String[] DETAIL_COLUMNS_SHARED = {
            NewzContract.SharedEntry.TABLE_NAME + "." + NewzContract.SharedEntry._ID,
            NewzContract.SharedEntry.ABSTRACT,
            NewzContract.SharedEntry.SECTION,
            NewzContract.SharedEntry.POST_URL,
            NewzContract.SharedEntry.AUTHOR,
            NewzContract.SharedEntry.DATE,
            NewzContract.SharedEntry.THUMB_URL,
            NewzContract.SharedEntry.TITLE,
    };

    private static final String[] DETAIL_COLUMNS_VIEWED= {
            NewzContract.ViewedEntry.TABLE_NAME + "." + NewzContract.ViewedEntry._ID,
            NewzContract.ViewedEntry.ABSTRACT,
            NewzContract.ViewedEntry.SECTION,
            NewzContract.ViewedEntry.POST_URL,
            NewzContract.ViewedEntry.AUTHOR,
            NewzContract.ViewedEntry.DATE,
            NewzContract.ViewedEntry.THUMB_URL,
            NewzContract.ViewedEntry.TITLE,
    };
    private String NEWZ_SHARE_HASHTAG = "#Newz";

    private String postUrl;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }



    public void onTypeChanged( String newType ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            switch (newType){
                case NewzContract.PATH_EMAILED:
                    uri = NewzContract.EmailedEntry.buildNewzUri();
                    break;
                case NewzContract.PATH_VIEWED:
                    uri = NewzContract.ViewedEntry.buildNewzUri();
                    break;
                case NewzContract.PATH_SHARED:
                    uri = NewzContract.SharedEntry.buildNewzUri();
                    break;
            }
            mUri = uri;
            mType = newType;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            CursorLoader c = null;
            switch (mType){
                case NewzContract.PATH_EMAILED:
                    c = new CursorLoader(
                            getActivity(),
                            mUri,
                            DETAIL_COLUMNS_EMAILED,
                            null,
                            null,
                            null
                    );
                    break;
                case NewzContract.PATH_SHARED:
                    c = new CursorLoader(
                            getActivity(),
                            mUri,
                            DETAIL_COLUMNS_SHARED,
                            null,
                            null,
                            null
                    );
                    break;
                case NewzContract.PATH_VIEWED:
                    c = new CursorLoader(
                            getActivity(),
                            mUri,
                            DETAIL_COLUMNS_VIEWED,
                            null,
                            null,
                            null
                    );
                    break;
            }
            return c;
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        mType = Utility.getNewzType(getActivity());
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mImage = (ImageView) rootView.findViewById(R.id.imageView);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        mAuthor = (TextView) rootView.findViewById(R.id.author);
        mDate= (TextView) rootView.findViewById(R.id.date);
        mSection = (TextView) rootView.findViewById(R.id.section);
        mAbstract = (TextView) rootView.findViewById(R.id.abs);
        mButton = (Button) rootView.findViewById(R.id.button);
        return rootView;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read weather condition ID from cursor

            int newzID = data.getInt(COL_NEWZ_ID);
            String abs = data.getString(COL_ABSTRACT);
            String aut = data.getString(COL_AUTHOR);
            String date = data.getString(COL_DATE);
            postUrl = data.getString(COL_POST_URL);
            String thumbUrl = data.getString(COL_THUMB_URL);
            String title = data.getString(COL_TITLE);
            String section= data.getString(COL_SECTION);

            if (thumbUrl.length() > 0) {
                Picasso.with(getActivity()).load(thumbUrl).transform(new CircleTransform()).into(mImage);
            }
            else {
                Picasso.with(getActivity()).load(R.drawable.icon).into(mImage);
            }
            mAbstract.setText(abs);
            mAuthor.setText(aut);
            mDate.setText(date);
            mTitle.setText(title);
            mSection.setText(section);

            // We still need this for the share intent
            mNewz = String.format("%s - %s - %s", title, section, postUrl);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareNewzIntent());
            }
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(),WebViewActivity.class);
                    i.putExtra("url", postUrl);
                    startActivity(i);
                }
            });
            mButton.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mNewz != null) {
            mShareActionProvider.setShareIntent(createShareNewzIntent());
        }
    }

    private Intent createShareNewzIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mNewz + " " + NEWZ_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
}
