package com.quarkworks.apartmentgroceries.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.RUser;

/**
 * Created by zz on 10/15/15.
 */
public class UserCell extends RelativeLayout{

    private static final String TAG = UserCell.class.getSimpleName();

    private TextView nameTextView;

    public UserCell(Context context) {
        super(context);
        initialize();
    }

    public UserCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public UserCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(R.layout.user_cell, this);
        nameTextView = (TextView) findViewById(R.id.user_cell_name_id);
    }

    public void setViewData(RUser user){
        nameTextView.setText(user.getName());
    }
}
