package com.quarkworks.apartmentgroceries.grocery;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.RGroceryPhoto;
import com.quarkworks.apartmentgroceries.service.models.RUser;
import com.quarkworks.apartmentgroceries.user.UserDetailActivity;

import io.realm.RealmResults;

/**
 * Created by zz on 10/22/15.
 */
public class GroceryCardPagerFragment extends Fragment {
    private static final String TAG = GroceryCardPagerFragment.class.getSimpleName();

    private static final String GROCERY_ID = "groceryId";

    private String groceryId;
    private RGroceryItem rGroceryItem;
    private RealmResults<RGroceryPhoto> groceryPhotos;
    private ImageAdapter imageAdapter;

    public static GroceryCardPagerFragment newInstance(String groceryId) {
        GroceryCardPagerFragment groceryCardPagerFragment = new GroceryCardPagerFragment();
        Bundle args = new Bundle();
        args.putString(GROCERY_ID, groceryId);
        groceryCardPagerFragment.setArguments(args);

        return groceryCardPagerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groceryId = getArguments().getString(GROCERY_ID);

        rGroceryItem = DataStore.getInstance().getRealm().where(RGroceryItem.class)
                .equalTo(GROCERY_ID, groceryId).findFirst();

        groceryPhotos = DataStore.getInstance().getRealm()
                .where(RGroceryPhoto.class).equalTo(RGroceryPhoto.RealmKeys.GROCERY_ID,
                        rGroceryItem.getGroceryId()).findAll();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.grocery_card_pager_fragment, container, false);

        /*
            References
         */
        TextView nameTextView = (TextView)rootView.findViewById(R.id.grocery_card_pager_fragment_grocery_item_name_id);
        TextView createdByTextView = (TextView) rootView.findViewById(R.id.grocery_card_pager_fragment_grocery_item_created_by_id);
        GridView photoGridView = (GridView) rootView.findViewById(R.id.grocery_card_pager_grid_view_id);

        /*
            Set view data
         */
        nameTextView.setText(rGroceryItem.getName());
        RUser rUser = DataStore.getInstance().getRealm()
                .where(RUser.class).equalTo(RUser.RealmKeys.USER_ID,
                        rGroceryItem.getCreatedBy()).findFirst();
        if (rUser != null) {
            createdByTextView.setText(rUser.getUsername());
        }

        imageAdapter = new ImageAdapter(getContext(), groceryPhotos);
        photoGridView.setAdapter(imageAdapter);

        /*
            Set view OnClickListener
         */
        nameTextView.setOnClickListener(groceryItemNameOnClick);
        createdByTextView.setOnClickListener(createByOnClick);

        return rootView;
    }

    private View.OnClickListener groceryItemNameOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GroceryItemDetailActivity.newIntent(getContext(), groceryId);
        }
    };

    private View.OnClickListener createByOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UserDetailActivity.newIntent(getContext(), rGroceryItem.getCreatedBy());
        }
    };

    private class ImageAdapter extends BaseAdapter {
        private Context context;
        private RealmResults<RGroceryPhoto> groceryPhotos;

        public ImageAdapter(Context context, RealmResults<RGroceryPhoto> groceryPhotos) {
            this.context = context;
            this.groceryPhotos = groceryPhotos;

        }

        public int getCount() {
            return groceryPhotos.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            View rootView = View.inflate(context, R.layout.grocery_pager_photo_grid_view_item, null);

            imageView = (ImageView) rootView.findViewById(R.id.grocery_pager_grid_view_image_view_id);

            String photoUrl = groceryPhotos.get(position).getUrl();
            Glide.with(getContext())
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_launcher)
                    .centerCrop()
                    .crossFade()
                    .into(imageView);

            return rootView;
        }
    }
}
