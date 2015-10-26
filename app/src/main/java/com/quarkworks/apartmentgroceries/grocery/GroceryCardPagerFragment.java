package com.quarkworks.apartmentgroceries.grocery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.user.UserDetailActivity;

/**
 * Created by zz on 10/22/15.
 */
public class GroceryCardPagerFragment extends Fragment {
    private static final String TAG = GroceryCardPagerFragment.class.getSimpleName();

    private static final String GROCERY_ID = "groceryId";
    private String groceryId;
    private RGroceryItem rGroceryItem;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.grocery_card_pager_fragment, container, false);

        /**
         * Get view references
         */
        TextView nameTextView = (TextView)rootView.findViewById(R.id.grocery_card_pager_fragment_grocery_item_name_id);
        TextView createdByTextView = (TextView) rootView.findViewById(R.id.grocery_card_pager_fragment_grocery_item_created_by_id);

        /**
         * Set view data
         */
        nameTextView.setText(rGroceryItem.getName());
        createdByTextView.setText(rGroceryItem.getCreatedBy());

        /**
         * Set view OnClickListener
         */
        nameTextView.setOnClickListener(groceryItemNameOnClick);
        createdByTextView.setOnClickListener(createByOnClick);

        return rootView;
    }

    public View.OnClickListener groceryItemNameOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GroceryItemDetailActivity.newIntent(getContext(), groceryId);
        }
    };

    public View.OnClickListener createByOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UserDetailActivity.newIntent(getContext(), rGroceryItem.getCreatedBy());
        }
    };
}
