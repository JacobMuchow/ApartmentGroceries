package com.quarkworks.apartmentgroceries.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;

/**
 * Created by zz on 10/14/15.
 */
public class GroceryCell extends RelativeLayout{

    private static final String TAG = GroceryCell.class.getSimpleName();

    private TextView nameTextView;
    private TextView createdByTextView;

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
    }

    public void setViewData(RGroceryItem groceryItem){
        nameTextView.setText(groceryItem.getName());
        createdByTextView.setText(groceryItem.getCreatedBy());
    }
}
