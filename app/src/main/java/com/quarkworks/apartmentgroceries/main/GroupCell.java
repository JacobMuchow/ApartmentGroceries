package com.quarkworks.apartmentgroceries.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.RGroup;

/**
 * Created by zz on 10/16/15.
 */
public class GroupCell extends RelativeLayout{

    private static final String TAG = GroupCell.class.getSimpleName();

    private TextView nameTextView;

    public GroupCell(Context context) {
        super(context);
        initialize();
    }

    public GroupCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public GroupCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(R.layout.group_cell, this);
        nameTextView = (TextView) findViewById(R.id.group_cell_name_id);
    }

    public void setViewData(RGroup group){
        nameTextView.setText(group.getName());
    }
}
