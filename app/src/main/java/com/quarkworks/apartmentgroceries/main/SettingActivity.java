package com.quarkworks.apartmentgroceries.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.auth.LoginActivity;
import com.quarkworks.apartmentgroceries.group.GroupActivity;
import com.quarkworks.apartmentgroceries.service.SyncUser;
import com.quarkworks.apartmentgroceries.service.models.RUser;
import com.quarkworks.apartmentgroceries.user.UserDetailActivity;

import bolts.Continuation;
import bolts.Task;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = AppCompatActivity.class.getSimpleName();

    /*
        References
     */
    private Toolbar toolbar;
    private TextView titleTextView;
    private TextView groupTextView;
    private TextView logoutTextView;
    private TextView editProfileTextView;

    public static void newIntent(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);

        /**
         * Get view references
         */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar_id);
        titleTextView = (TextView) toolbar.findViewById(R.id.toolbar_title_id);
        groupTextView = (TextView) findViewById(R.id.setting_group_text_view_id);
        logoutTextView = (TextView) findViewById(R.id.setting_logout_text_view_id);
        editProfileTextView = (TextView) findViewById(R.id.setting_profile_text_view_id);

        /**
         * Set view data
         */
        titleTextView.setText(getString(R.string.title_activity_setting));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /**
         * Set view on click
         */
        groupTextView.setOnClickListener(groupTextViewOnClick);
        logoutTextView.setOnClickListener(logoutTextViewOnClick);
        editProfileTextView.setOnClickListener(editProfileTextViewOnClick);
    }

    private View.OnClickListener groupTextViewOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GroupActivity.newIntent(SettingActivity.this);
        }
    };

    private View.OnClickListener logoutTextViewOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Continuation<Void, Void> logoutOnSuccess = new Continuation<Void, Void>() {
                @Override
                public Void then(Task<Void> task) throws Exception {
                    if (task.isFaulted()) {
                        Log.e(TAG, task.getError().toString());
                        return null;
                    }

                    SharedPreferences sharedPreferences =
                            getSharedPreferences(getString(R.string.login_or_sign_up_session), 0);
                    sharedPreferences.edit().clear().apply();
                    LoginActivity.newIntent(SettingActivity.this);

                    return null;
                }
            };
            SyncUser.logout().continueWith(logoutOnSuccess);
        }
    };

    private View.OnClickListener editProfileTextViewOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences sharedPreferences = getApplication()
                    .getSharedPreferences(getString(R.string.login_or_sign_up_session), 0);
            String userId = sharedPreferences.getString(RUser.JsonKeys.USER_ID, null);
            UserDetailActivity.newIntent(SettingActivity.this, userId);
        }
    };
}
