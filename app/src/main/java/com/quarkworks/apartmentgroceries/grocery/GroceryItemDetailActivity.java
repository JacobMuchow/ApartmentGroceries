package com.quarkworks.apartmentgroceries.grocery;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.user.UserDetailActivity;

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
        RGroceryItem groceryItem = DataStore.getInstance().getRealm()
                .where(RGroceryItem.class).equalTo("groceryId", groceryId).findFirst();

        createdByTextView.setText(groceryItem.getCreatedBy());
        groceryItemNameTextView.setText(groceryItem.getName());
        groupNameTextView.setText(groceryItem.getGroupName());

        final String username = groceryItem.getCreatedBy();
        createdByTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroceryItemDetailActivity.this, UserDetailActivity.class);
                intent.putExtra(GroceryCell.USERNAME, username);
                startActivity(intent);
            }
        });

    }
}
