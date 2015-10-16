package com.quarkworks.apartmentgroceries.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;

import io.realm.RealmResults;

public class GroceryCardPagerActivity extends AppCompatActivity {
    private static final String TAG = GroceryCardPagerActivity.class.getSimpleName();

    private static int NUM_PAGES = 0;
    private ViewPager viewPager;
    private GroceryCardPagerAdapter groceryCardPagerAdapter;

    public static RealmResults<RGroceryItem> groceryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grocery_card_pager_activity);

        groceryItems = DataStore.getInstance().getRealm().where(RGroceryItem.class).findAll();
        NUM_PAGES = groceryItems.size();

        viewPager = (ViewPager) findViewById(R.id.grocery_card_pager_view_pager_id);
        groceryCardPagerAdapter = new GroceryCardPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(groceryCardPagerAdapter);

        int position = getIntent().getExtras().getInt(HomeActivity.POSITION);
        viewPager.setCurrentItem(position);

    }

    public static class GroceryCardPagerAdapter extends FragmentStatePagerAdapter {
        public GroceryCardPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return GroceryCardPagerFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public static class GroceryCardPagerFragment extends Fragment {
        private int fPosition;
        private RealmResults<RGroceryItem> fGroceryItems;

        static GroceryCardPagerFragment newInstance(int num) {
            GroceryCardPagerFragment groceryCardPagerFragment = new GroceryCardPagerFragment();
            Bundle args = new Bundle();
            args.putInt("num", num);
            groceryCardPagerFragment.setArguments(args);

            return groceryCardPagerFragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            fGroceryItems = DataStore.getInstance().getRealm().where(RGroceryItem.class).findAll();
            fPosition = getArguments() != null ? getArguments().getInt("num") : 0;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.grocery_card_pager_fragment, container, false);
            TextView nameTextView = (TextView)rootView.findViewById(R.id.grocery_card_pager_fragment_grocery_item_name_id);
            nameTextView.setText(fGroceryItems.get(fPosition).getName());
            return rootView;
        }
    }
}
