package com.quarkworks.apartmentgroceries.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;

public class GroceryItemDetailActivity extends AppCompatActivity {
    private static final String TAG = GroceryItemDetailActivity.class.getSimpleName();

    /*
        References
     */
    private TextView createdByTextView;
    private TextView groceryItemNameTextView;
    private TextView groupNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grocery_item_detail_activity);

        createdByTextView = (TextView) findViewById(R.id.grocery_item_detail_created_id);
        groceryItemNameTextView = (TextView) findViewById(R.id.grocery_item_detail_name_id);
        groupNameTextView = (TextView) findViewById(R.id.grocery_item_detail_group_id);

        String groceryId = getIntent().getExtras().getString(GroceryCardPagerActivity.GROCER_ITEM_ID);
        RGroceryItem rGroceryItem = DataStore.getInstance().getRealm()
                .where(RGroceryItem.class).equalTo("groceryId", groceryId).findFirst();

        createdByTextView.setText(rGroceryItem.getCreatedBy());
        groceryItemNameTextView.setText(rGroceryItem.getName());
        groupNameTextView.setText(rGroceryItem.getGroupName());
    }
}
