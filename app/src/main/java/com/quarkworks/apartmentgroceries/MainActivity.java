package com.quarkworks.apartmentgroceries;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.quarkworks.apartmentgroceries.auth.LoginActivity;
import com.quarkworks.apartmentgroceries.main.HomeActivity;
import com.quarkworks.apartmentgroceries.service.models.RUser;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.login_or_sign_up_session), 0);
        String sessionToken = sharedPreferences.getString(RUser.JsonKeys.GROUP_ID, null);

        if (TextUtils.isEmpty(sessionToken)) {
            LoginActivity.newIntent(this);
        } else {
            HomeActivity.newIntent(this);
        }
    }
}
