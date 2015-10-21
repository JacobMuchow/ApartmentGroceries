package com.quarkworks.apartmentgroceries.grocery;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.SyncUser;
import com.quarkworks.apartmentgroceries.service.Utilities;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.RUser;
import com.quarkworks.apartmentgroceries.user.UserDetailActivity;

import bolts.Continuation;
import bolts.Task;
import io.realm.RealmResults;

/**
 * Created by zz on 10/14/15.
 */
public class GroceryCell extends RelativeLayout {

    private static final String TAG = GroceryCell.class.getSimpleName();
    public static final String POSITION = "POSITION";
    public static final String USERNAME = "USERNAME";

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

    public void setViewData(final RGroceryItem groceryItem, final int position) {
        nameTextView.setText(groceryItem.getName());
        createdAtTextView.setText(Utilities.getReadableDate(groceryItem.getCreatedAt(), null, null));

        String purchasedBy = groceryItem.getPurchasedBy();
        if (TextUtils.isEmpty(purchasedBy)) {
            statusTextView.setText(getResources().getString(R.string.grocery_cell_item_status_open));
        } else {
            purchasedByTextView.setText(purchasedBy);
            statusTextView.setText(getResources().getString(R.string.grocery_cell_item_status_closed));
        }
        statusTextView.setTextColor(getResources().getColor(R.color.colorPrimary));

        photoImageView.setImageResource(R.drawable.ic_launcher);

        final RUser rUser = DataStore.getInstance().getRealm().where(RUser.class)
                .equalTo(SyncUser.JsonKeys.USER_ID, groceryItem.getCreatedBy()).findFirst();
        createdByTextView.setText(rUser.getUsername());

        Glide.with(getContext())
                .load(rUser.getUrl())
                .placeholder(R.drawable.ic_launcher)
                .centerCrop()
                .crossFade()
                .into(photoImageView);

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
