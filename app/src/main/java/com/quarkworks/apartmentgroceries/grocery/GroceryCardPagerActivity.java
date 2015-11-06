package com.quarkworks.apartmentgroceries.grocery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;

import io.realm.RealmResults;

public class GroceryCardPagerActivity extends AppCompatActivity {
    private static final String TAG = GroceryCardPagerActivity.class.getSimpleName();

    public static final String POSITION = "position";
    private GroceryCardPagerAdapter groceryCardPagerAdapter;
    private static RealmResults<RGroceryItem> groceryItems;

    /*
        References
     */
    private ViewPager viewPager;
    private int curPosition;

    public static void newIntent(Context context, int position) {
        Intent intent = new Intent(context, GroceryCardPagerActivity.class);
        intent.putExtra(POSITION, position);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grocery_card_pager_activity);

        /*
            Get view references
         */
        viewPager = (ViewPager) findViewById(R.id.grocery_card_pager_view_pager_id);

        /*
            Set view data
         */
        groceryItems = DataStore.getInstance().getRealm().where(RGroceryItem.class).findAll();
        groceryItems.sort(RGroceryItem.RealmKeys.CREATED_AT, false);

        groceryCardPagerAdapter = new GroceryCardPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(groceryCardPagerAdapter);

        curPosition = getIntent().getIntExtra(POSITION, 0);
        viewPager.setCurrentItem(curPosition);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setClipChildren(false);
        viewPager.setPageMarginDrawable(null);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT > 21) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private static class GroceryCardPagerAdapter extends FragmentStatePagerAdapter {
        public GroceryCardPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            String groceryId = groceryItems.get(position).getGroceryId();
            return GroceryCardPagerFragment.newInstance(groceryId);
        }

        @Override
        public int getCount() {
            return groceryItems.size();
        }
    }
}
