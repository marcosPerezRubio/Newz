package perez.marcos.com.newz;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by marcos on 10/06/2015.
 */
public class NewzAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_FIRST = 0;
    private static final int VIEW_TYPE_OTHER = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;
    private boolean mTwoPane;
    private String postUrl;
    private String thumbUrl;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView titleView;
        public final TextView descriptionView;
        public final TextView sectionView;
        public final TextView dateView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_first_icon);
            titleView = (TextView) view.findViewById(R.id.list_first_title);
            descriptionView = (TextView) view.findViewById(R.id.list_first_desc);
            sectionView = (TextView) view.findViewById(R.id.list_first_section);
            dateView = (TextView)view.findViewById(R.id.list_first_date);
        }
    }

    public NewzAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_FIRST: {
                layoutId = R.layout.list_item_first_newz;
                break;
            }
            case VIEW_TYPE_OTHER: {
                if (mTwoPane) {
                    layoutId = R.layout.list_item_newz;
                }
                else {
                    layoutId = R.layout.list_item_newz_img;
                }
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        String abs = cursor.getString(NewzFragment.COL_NEWZ_ABSTRACT);
        String title = cursor.getString(NewzFragment.COL_NEWZ_TITLE);
        String section = cursor.getString(NewzFragment.COL_NEWZ_SECTION);
        String date = cursor.getString(NewzFragment.COL_NEWZ_DATE);
        postUrl = cursor.getString(NewzFragment.COL_NEWZ_POST_URL);
        thumbUrl = cursor.getString(NewzFragment.COL_NEWZ_THUMB_URL);
        switch (viewType) {
            case VIEW_TYPE_FIRST: {
                if (thumbUrl.length() > 0 ) {
                    Picasso.with(context).load(thumbUrl).transform(new CircleTransform()).into(viewHolder.iconView);
                }
                else {
                    Picasso.with(context).load(R.drawable.icon).into(viewHolder.iconView);
                }
                viewHolder.titleView.setText(title);
                viewHolder.descriptionView.setText(abs);
                viewHolder.sectionView.setText(section);
                viewHolder.dateView.setText(date);

                break;
            }

            case VIEW_TYPE_OTHER: {
                if (!mTwoPane) {
                    if (thumbUrl.length() > 0) {
                        Picasso.with(context).load(thumbUrl).transform(new CircleTransform()).into(viewHolder.iconView);
                    } else {
                        Picasso.with(context).load(R.drawable.icon).into(viewHolder.iconView);
                    }
                }
                viewHolder.titleView.setText(title);
                viewHolder.sectionView.setText(section);
                viewHolder.dateView.setText(date);
                break;
            }
        }

    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    public void setTwoPaneLayout(boolean twoPane){
        mTwoPane = twoPane;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_FIRST : VIEW_TYPE_OTHER;
    }



    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }


}
