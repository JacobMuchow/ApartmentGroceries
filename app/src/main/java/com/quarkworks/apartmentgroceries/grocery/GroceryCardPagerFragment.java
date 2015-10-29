package com.quarkworks.apartmentgroceries.grocery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
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
import com.quarkworks.apartmentgroceries.service.SyncGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.user.UserDetailActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by zz on 10/22/15.
 */
public class GroceryCardPagerFragment extends Fragment {
    private static final String TAG = GroceryCardPagerFragment.class.getSimpleName();

    private static final String GROCERY_ID = "groceryId";
    private String groceryId;
    private RGroceryItem rGroceryItem;
    private ArrayList<String> photoUrlList;
    private ImageAdapter imageAdapter;

    /**
     * References
     */
    private TextView nameTextView;
    private TextView createdByTextView;
    private GridView photoGridView;

    static GroceryCardPagerFragment newInstance(String groceryId) {
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


        Continuation<JSONObject, Void> getGroceryPhotoUrl = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in getGroceryPhoto", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();
                if (jsonObject == null) {
                    return null;
                }

                JSONArray photoUrlArray = jsonObject.getJSONArray("results");
                for (int i = 0; i < photoUrlArray.length(); i++) {
                    String url = photoUrlArray.getJSONObject(i).getJSONObject("photo").getString("url");
                    if (!TextUtils.isEmpty(url)) photoUrlList.add(url);
                }

                if (photoUrlList != null) imageAdapter.notifyDataSetChanged();

                return null;
            }
        };

        Continuation<Void, Void> checkGroceryPhotoResult = new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if(task.isFaulted()) {
                    Log.e(TAG, "Error:" + task.getError());
                }
                return null;
            }
        };

        SyncGroceryItem.getGroceryPhotoByGroceryId(rGroceryItem.getGroceryId())
                .continueWith(getGroceryPhotoUrl, Task.UI_THREAD_EXECUTOR)
                .continueWith(checkGroceryPhotoResult);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.grocery_card_pager_fragment, container, false);

        /**
         * Get view references
         */
        nameTextView = (TextView)rootView.findViewById(R.id.grocery_card_pager_fragment_grocery_item_name_id);
        createdByTextView = (TextView) rootView.findViewById(R.id.grocery_card_pager_fragment_grocery_item_created_by_id);
        photoGridView = (GridView) rootView.findViewById(R.id.grocery_card_pager_grid_view_id);

        /**
         * Set view data
         */
        nameTextView.setText(rGroceryItem.getName());
        createdByTextView.setText(rGroceryItem.getCreatedBy());

        if (photoUrlList == null) photoUrlList = new ArrayList<>();
        imageAdapter = new ImageAdapter(getContext(), photoUrlList);
        photoGridView.setAdapter(imageAdapter);

        /**
         * Set view OnClickListener
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
        private ArrayList<String> photoUrlList;

        public ImageAdapter(Context context, ArrayList<String> photoList) {
            if (photoList == null) photoList = new ArrayList<>();
            this.context = context;
            this.photoUrlList = photoList;

        }

        public int getCount() {
            return photoUrlList.size();
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

            String photoUrl = photoUrlList.get(position);
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
