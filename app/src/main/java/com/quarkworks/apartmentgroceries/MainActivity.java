package com.quarkworks.apartmentgroceries;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.quarkworks.apartmentgroceries.auth.LoginActivity;
import com.quarkworks.apartmentgroceries.auth.SignupActivity;
import com.quarkworks.apartmentgroceries.auth.UserActivity;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
            case R.id.action_login:
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_signup:
                intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_user_update:
                intent = new Intent(this, UserActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_settings:
                return true;

        }

        return super.onOptionsItemSelected(item);
    }
}
