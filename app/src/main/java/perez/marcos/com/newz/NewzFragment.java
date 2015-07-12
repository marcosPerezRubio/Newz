package perez.marcos.com.newz;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import perez.marcos.com.newz.data.NewzContract;
import perez.marcos.com.newz.sync.NewzSyncAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewzFragment#} factory method to
 * create an instance of this fragment.
 */
public class NewzFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = NewzFragment.class.getSimpleName();
    private NewzAdapter adapter;
    private boolean mTwoPane;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseFirstLayout;

    private static final String SELECTED_KEY = "selected_position";

    private static final int NEWZ_LOADER = 0;
    private static final String[] NEWZ_EMAILED_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            NewzContract.EmailedEntry.TABLE_NAME + "." + NewzContract.EmailedEntry._ID,
            NewzContract.EmailedEntry.TITLE,
            NewzContract.EmailedEntry.DATE,
            NewzContract.EmailedEntry.ABSTRACT,
            NewzContract.EmailedEntry.POST_URL,
            NewzContract.EmailedEntry.SECTION,
            NewzContract.EmailedEntry.THUMB_URL,
    };

    private static final String[] NEWZ_VIEWED_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            NewzContract.ViewedEntry.TABLE_NAME + "." + NewzContract.ViewedEntry._ID,
            NewzContract.ViewedEntry.TITLE,
            NewzContract.ViewedEntry.DATE,
            NewzContract.ViewedEntry.ABSTRACT,
            NewzContract.ViewedEntry.POST_URL,
            NewzContract.ViewedEntry.SECTION,
            NewzContract.ViewedEntry.THUMB_URL,
    };

    private static final String[] NEWZ_SHARED_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            NewzContract.SharedEntry.TABLE_NAME + "." + NewzContract.SharedEntry._ID,
            NewzContract.SharedEntry.TITLE,
            NewzContract.SharedEntry.DATE,
            NewzContract.SharedEntry.ABSTRACT,
            NewzContract.SharedEntry.POST_URL,
            NewzContract.SharedEntry.SECTION,
            NewzContract.SharedEntry.THUMB_URL
    };

    static final int COL_NEWZ_ID = 0;
    static final int COL_NEWZ_TITLE = 1;
    static final int COL_NEWZ_DATE = 2;
    static final int COL_NEWZ_ABSTRACT = 3;
    static final int COL_NEWZ_POST_URL = 4;
    static final int COL_NEWZ_SECTION = 5;
    static final int COL_NEWZ_THUMB_URL = 6;

    public void onTypeOrDaysChanged() {
        updateNewz();
        getLoaderManager().restartLoader(NEWZ_LOADER, null, this);
    }

    private void updateNewz() {
        NewzSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        // Sort order:  Ascending, by date.
        //String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        String newz = Utility.getNewzType(getActivity());
        Uri newzuri;
        CursorLoader c = null;
        switch (newz) {
            //De la uri newz uri, quiero que cojas las columnas definidas en NEWZ_X_COLUMN
            case NewzContract.PATH_EMAILED:
                newzuri = NewzContract.EmailedEntry.buildNewzUri();
                c = new CursorLoader(getActivity(),
                        newzuri,
                        NEWZ_EMAILED_COLUMNS,
                        null,
                        null,
                        null);
                break;
            case NewzContract.PATH_VIEWED:
                newzuri = NewzContract.ViewedEntry.buildNewzUri();
                c = new CursorLoader(getActivity(),
                        newzuri,
                        NEWZ_VIEWED_COLUMNS,
                        null,
                        null,
                        null);
                break;
            case NewzContract.PATH_SHARED:
                newzuri = NewzContract.SharedEntry.buildNewzUri();
                c = new CursorLoader(getActivity(),
                        newzuri,
                        NEWZ_SHARED_COLUMNS,
                        null,
                        null,
                        null);
                break;
        }
        return c;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selection.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public NewzFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // The ForecastAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        adapter = new NewzAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        //TODO aqui se inicializaba el syncadapter

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_newz);
        mListView.setAdapter(adapter);
        // We'll call our MainActivity
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String mType = Utility.getNewzType(getActivity());
                    String id = cursor.getString(COL_NEWZ_ID);
                    Long id2 = Long.parseLong(id);
                    switch (mType){
                        case NewzContract.PATH_VIEWED:
                            ((Callback) getActivity())
                                    .onItemSelected(NewzContract.ViewedEntry.buildViewedUri(id2));
                            break;
                        case NewzContract.PATH_EMAILED:
                            ((Callback) getActivity())
                                    .onItemSelected(NewzContract.EmailedEntry.buildEmailedUri(id2));
                            break;
                        case NewzContract.PATH_SHARED:
                            ((Callback) getActivity())
                                    .onItemSelected(NewzContract.SharedEntry.buildSharedUri(id2));
                            break;
                    }
                }
                mPosition = position;

            }


        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        adapter.setUseTodayLayout(mUseFirstLayout);
        return rootView;
    }

    public void setUseFirstLayout(boolean useFirstLayout) {
        mUseFirstLayout = useFirstLayout;
        if (adapter != null) {
            adapter.setUseTodayLayout(mUseFirstLayout);
            adapter.setTwoPaneLayout(!mUseFirstLayout);
        }
    }
}
