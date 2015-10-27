package com.quarkworks.apartmentgroceries.grocery;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.user.UserDetailActivity;

public class GroceryItemDetailActivity extends AppCompatActivity {
    private static final String TAG = GroceryItemDetailActivity.class.getSimpleName();

    private static final String GROCERY_ID = "groceryId";
    private RGroceryItem groceryItem;

    /*
        References
     */
    private TextView createdByTextView;
    private TextView groceryItemNameTextView;
    private TextView groupNameTextView;

    public static void newIntent(Context context, String groceryId) {
        Intent intent = new Intent(context, GroceryItemDetailActivity.class);
        intent.putExtra(GROCERY_ID, groceryId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grocery_item_detail_activity);

        /**
         * Get view references
         */
        createdByTextView = (TextView) findViewById(R.id.grocery_item_detail_created_id);
        groceryItemNameTextView = (TextView) findViewById(R.id.grocery_item_detail_name_id);
        groupNameTextView = (TextView) findViewById(R.id.grocery_item_detail_group_id);

        String groceryId = getIntent().getStringExtra(GROCERY_ID);
        groceryItem = DataStore.getInstance().getRealm()
                .where(RGroceryItem.class).equalTo(GROCERY_ID, groceryId).findFirst();

        /**
         * Set view data
         */
        createdByTextView.setText(groceryItem.getCreatedBy());
        groceryItemNameTextView.setText(groceryItem.getName());
        groupNameTextView.setText(groceryItem.getGroupName());

        /**
         * Set view OnClickListener
         */
        createdByTextView.setOnClickListener(createdByOnClickListener);

    }

    private View.OnClickListener createdByOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UserDetailActivity.newIntent(GroceryItemDetailActivity.this, groceryItem.getCreatedBy());
        }
    };
}
