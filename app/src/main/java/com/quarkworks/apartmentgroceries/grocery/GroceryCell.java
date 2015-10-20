package com.quarkworks.apartmentgroceries.grocery;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.user.UserDetailActivity;

/**
 * Created by zz on 10/14/15.
 */
public class GroceryCell extends RelativeLayout{

    private static final String TAG = GroceryCell.class.getSimpleName();
    public static final String POSITION = "POSITION";
    public static final String USERNAME = "USERNAME";

    private TextView nameTextView;
    private TextView createdByTextView;
    private TextView purchasedByTextView;

    public GroceryCell(Context context) {
        super(context);
        initialize();
    }

    public GroceryCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public GroceryCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(R.layout.grocery_cell, this);
        nameTextView = (TextView) findViewById(R.id.grocery_cell_grocery_name_id);
        createdByTextView = (TextView) findViewById(R.id.grocery_cell_created_by_id);
        purchasedByTextView = (TextView) findViewById(R.id.grocery_cell_purchased_by_id);
    }

    public void setViewData(final RGroceryItem groceryItem, final int position){
        nameTextView.setText(groceryItem.getName());
        createdByTextView.setText(groceryItem.getCreatedBy());
        purchasedByTextView.setText(groceryItem.getPurchasedBy());

        nameTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GroceryCardPagerActivity.class);
                intent.putExtra(POSITION, position);
                getContext().startActivity(intent);
            }
        });

        createdByTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserDetailActivity.class);
                intent.putExtra(USERNAME, groceryItem.getCreatedBy());
                getContext().startActivity(intent);
            }
        });
        purchasedByTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserDetailActivity.class);
                intent.putExtra(USERNAME, groceryItem.getPurchasedBy());
                getContext().startActivity(intent);
            }
        });

    }
}
