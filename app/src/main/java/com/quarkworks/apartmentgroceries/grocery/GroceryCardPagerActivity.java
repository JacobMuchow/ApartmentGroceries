package com.quarkworks.apartmentgroceries.grocery;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.user.UserDetailActivity;

import io.realm.RealmResults;

public class GroceryCardPagerActivity extends AppCompatActivity {
    private static final String TAG = GroceryCardPagerActivity.class.getSimpleName();

    public static final String GROCER_ITEM_ID = "groceryId";
    private static int NUM_PAGES = 0;
    private ViewPager viewPager;
    private GroceryCardPagerAdapter groceryCardPagerAdapter;

    public static RealmResults<RGroceryItem> groceryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grocery_card_pager_activity);

        /**
         * Get view references
         */
        viewPager = (ViewPager) findViewById(R.id.grocery_card_pager_view_pager_id);

        groceryItems = DataStore.getInstance().getRealm().where(RGroceryItem.class).findAll();
        NUM_PAGES = groceryItems.size();

        groceryCardPagerAdapter = new GroceryCardPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(groceryCardPagerAdapter);

        int position = getIntent().getExtras().getInt(GroceryCell.POSITION);
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
            TextView createdByTextView = (TextView) rootView.findViewById(R.id.grocery_card_pager_fragment_grocery_item_created_by_id);
            nameTextView.setText(fGroceryItems.get(fPosition).getName());
            createdByTextView.setText(fGroceryItems.get(fPosition).getCreatedBy());

            nameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), GroceryItemDetailActivity.class);
                    intent.putExtra(GROCER_ITEM_ID, fGroceryItems.get(fPosition).getGroceryId());
                    startActivity(intent);
                }
            });

            createdByTextView.setOnClickListener(new View.OnClickListener() {
                String username = fGroceryItems.get(fPosition).getCreatedBy();
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), UserDetailActivity.class);
                    intent.putExtra(GroceryCell.USERNAME, username);
                    getContext().startActivity(intent);
                }
            });

            return rootView;
        }
    }
}
