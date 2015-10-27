package com.quarkworks.apartmentgroceries.user;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.models.RUser;

public class UserDetailActivity extends AppCompatActivity {
    private static final String TAG = UserDetailActivity.class.getSimpleName();

    private static final String USER_ID = "userId";
    private String userId;

    /**
     * References
     */
    private Toolbar toolbar;
    private TextView titleTextView;
    private TextView editPhotoTextView;
    private TextView usernameTextView;
    private ImageView profileImageView;

    public static void newIntent(Context context, String userId) {
        Intent intent = new Intent(context, UserDetailActivity.class);
        intent.putExtra(USER_ID, userId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_detail_activity);

        userId = getIntent().getStringExtra(USER_ID);
        RUser rUser = DataStore.getInstance().getRealm().where(RUser.class)
                .equalTo(USER_ID, userId).findFirst();

        /**
         * Get view reference
         */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar_id);
        titleTextView = (TextView) toolbar.findViewById(R.id.toolbar_title_id);
        editPhotoTextView = (TextView) findViewById(R.id.user_detail_edit_photo_text_view_id);
        usernameTextView = (TextView) findViewById(R.id.user_detail_edit_username_text_view_id);
        profileImageView = (ImageView) findViewById(R.id.user_detail_profile_image_view_id);

        /**
         * Set view data
         */
        titleTextView.setText(getString(R.string.title_activity_user_detail));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        usernameTextView.setText(rUser.getUsername());

        Glide.with(this)
                .load(rUser.getUrl())
                .placeholder(R.drawable.ic_launcher)
                .centerCrop()
                .crossFade()
                .into(profileImageView);

        /**
         * Set view on click
         */
        editPhotoTextView.setOnClickListener(editPhotoTextViewOnClick());


    }

    private View.OnClickListener editPhotoTextViewOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoActivity.newIntent(UserDetailActivity.this, userId);
            }
        };
    }
}
