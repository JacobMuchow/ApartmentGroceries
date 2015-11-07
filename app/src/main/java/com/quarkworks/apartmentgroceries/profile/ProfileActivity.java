package com.quarkworks.apartmentgroceries.profile;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.grocery.GroceryCell;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.RUser;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();

    private static final String USER_ID = "userId";
    private String userId;

    public static void newIntent(Context context, String userId) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(USER_ID, userId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        /*
            Reference
         */
        final Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        final ImageView imageView = (ImageView) findViewById(R.id.profile_backdrop);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.profile_collapsing_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
            Set view data
         */
        userId = getIntent().getStringExtra(USER_ID);
        RUser rUser = DataStore.getInstance().getRealm().where(RUser.class)
                .equalTo(USER_ID, userId).findFirst();

        collapsingToolbar.setTitle(rUser.getUsername());
        Glide.with(this)
                .load(rUser.getUrl())
                .centerCrop()
                .crossFade()
                .into(imageView);


        final RealmResults<RGroceryItem> groceries = DataStore.getInstance().getRealm()
                .where(RGroceryItem.class).equalTo(RGroceryItem.RealmKeys.CREATED_BY, userId)
                .findAllSorted(RGroceryItem.RealmKeys.CREATED_AT,false);

        RealmBaseAdapter<RGroceryItem> realmBaseAdapter = new RealmBaseAdapter<RGroceryItem>(this, groceries, true) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                GroceryCell groceryCell = convertView != null ?
                        (GroceryCell) convertView : new GroceryCell(parent.getContext());
                groceryCell.setViewData(getItem(position), position);
                return groceryCell;
            }
        };

        ListView listView = (ListView) findViewById(R.id.profile_grocery_list_view_id);
        listView.setAdapter(realmBaseAdapter);
        setListViewHeightBasedOnChildren(listView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile_details:
                ProfileDetailActivity.newIntent(this, userId);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
