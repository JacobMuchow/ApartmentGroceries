package com.quarkworks.apartmentgroceries.grocery;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.Utilities;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.RUser;
import com.quarkworks.apartmentgroceries.user.UserDetailActivity;

/**
 * Created by zz on 10/14/15.
 */
public class GroceryCell extends RelativeLayout {
    private static final String TAG = GroceryCell.class.getSimpleName();

    private int position;
    private RGroceryItem rGroceryItem;

    private TextView nameTextView;
    private TextView createdByTextView;
    private TextView createdAtTextView;
    private TextView purchasedByTextView;
    private TextView statusTextView;
    private ImageView photoImageView;

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
        createdAtTextView = (TextView) findViewById(R.id.grocery_cell_created_at_id);
        purchasedByTextView = (TextView) findViewById(R.id.grocery_cell_purchased_by_id);
        statusTextView = (TextView) findViewById(R.id.grocery_cell_grocery_status_id);
        photoImageView = (ImageView) findViewById(R.id.grocery_cell_created_by_image_view_id);
    }

    public void setViewData(RGroceryItem groceryItem, int position) {
        this.position = position;
        this.rGroceryItem = groceryItem;

        nameTextView.setText(groceryItem.getName());
        createdAtTextView.setText(Utilities.getReadableDate(groceryItem.getCreatedAt(), null, null));

        if (TextUtils.isEmpty(groceryItem.getPurchasedBy())) {
            statusTextView.setText(getResources().getString(R.string.grocery_cell_item_status_open));
            purchasedByTextView.setText("");
        } else {
            statusTextView.setText(getResources().getString(R.string.grocery_cell_item_status_closed));
            RUser rUserPurchasedBy = DataStore.getInstance().getRealm().where(RUser.class)
                    .equalTo(RUser.RealmKeys.USER_ID, groceryItem.getPurchasedBy()).findFirst();
            if (rUserPurchasedBy != null) {
                purchasedByTextView.setText(rUserPurchasedBy.getUsername());
            } else {
                purchasedByTextView.setText("");
            }
        }

        statusTextView.setTextColor(getResources().getColor(R.color.colorPrimary));

        final RUser rUser = DataStore.getInstance().getRealm().where(RUser.class)
                .equalTo(RUser.JsonKeys.USER_ID, groceryItem.getCreatedBy()).findFirst();
        if (rUser != null) {
            createdByTextView.setText(rUser.getUsername());

            Glide.with(getContext())
                    .load(rUser.getUrl())
                    .placeholder(R.drawable.ic_launcher)
                    .centerCrop()
                    .crossFade()
                    .into(photoImageView);
        }

        /**
         * set view OnClickListener
         */
        nameTextView.setOnClickListener(groceryNameOnClick());
        createdByTextView.setOnClickListener(createdByOnClick());
        purchasedByTextView.setOnClickListener(purchasedByOnClick());
    }

    public View.OnClickListener groceryNameOnClick() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                GroceryCardPagerActivity.newIntent(getContext(), position);
            }
        };
    }

    public View.OnClickListener createdByOnClick() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDetailActivity.newIntent(getContext(), rGroceryItem.getCreatedBy());
            }
        };
    }

    public View.OnClickListener purchasedByOnClick() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDetailActivity.newIntent(getContext(), rGroceryItem.getPurchasedBy());
            }
        };
    }
}
