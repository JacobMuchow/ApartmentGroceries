package com.quarkworks.apartmentgroceries;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.quarkworks.apartmentgroceries.auth.LoginActivity;
import com.quarkworks.apartmentgroceries.main.HomeActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        SharedPreferences sharedPreferences = getApplication()
                .getSharedPreferences(getApplication().getString(R.string.login_or_sign_up_session), 0);
        String sessionToken = sharedPreferences.getString("sessionToken", null);

        Intent intent;
        if (TextUtils.isEmpty(sessionToken)) {
            intent = new Intent(MainActivity.this, LoginActivity.class);
        } else {
            intent = new Intent(MainActivity.this, HomeActivity.class);
        }
        startActivity(intent);
    }
}
