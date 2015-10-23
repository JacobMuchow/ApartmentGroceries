package com.quarkworks.apartmentgroceries.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.auth.LoginActivity;
import com.quarkworks.apartmentgroceries.group.GroupActivity;
import com.quarkworks.apartmentgroceries.service.SyncUser;

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
        groupTextView.setOnClickListener(groupTextViewOnClick());
        logoutTextView.setOnClickListener(logoutTextViewOnClick());
    }

    public View.OnClickListener groupTextViewOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupActivity.newIntent(SettingActivity.this);
            }
        };
    }

    public View.OnClickListener logoutTextViewOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Continuation<Boolean, Void> logoutOnSuccess = new Continuation<Boolean, Void>() {
                    @Override
                    public Void then(Task<Boolean> task) throws Exception {
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
    }
}
