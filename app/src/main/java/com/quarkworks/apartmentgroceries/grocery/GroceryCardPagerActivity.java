package com.quarkworks.apartmentgroceries.grocery;

import android.content.Context;
import android.content.Intent;
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
    private static int NUM_PAGES = 0;
    private ViewPager viewPager;
    private GroceryCardPagerAdapter groceryCardPagerAdapter;

    private static RealmResults<RGroceryItem> groceryItems;

    public static void newIntent(Context context, int position) {
        Intent intent = new Intent(context, GroceryCardPagerActivity.class);
        intent.putExtra(POSITION, position);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grocery_card_pager_activity);

        /**
         * Get view references
         */
        viewPager = (ViewPager) findViewById(R.id.grocery_card_pager_view_pager_id);
        viewPager.setPageTransformer(true, new zoomOutPageTransformer());

        groceryItems = DataStore.getInstance().getRealm().where(RGroceryItem.class).findAll();
        groceryItems.sort(RGroceryItem.RealmKeys.CREATED_AT, false);
        NUM_PAGES = groceryItems.size();

        groceryCardPagerAdapter = new GroceryCardPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(groceryCardPagerAdapter);

        int position = getIntent().getIntExtra(POSITION, 0);
        viewPager.setCurrentItem(position);
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
            return NUM_PAGES;
        }
    }

    private class zoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) {
                // left most
                view.setAlpha(0);

            } else if (position <= 1) {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float verticalMargin = pageHeight * (1 - scaleFactor) / 2;
                float horizontalMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horizontalMargin - verticalMargin / 2);
                } else {
                    view.setTranslationX(-horizontalMargin + verticalMargin / 2);
                }

                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else {
                // right most
                view.setAlpha(0);
            }
        }
    }
}
