package com.quarkworks.apartmentgroceries;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.quarkworks.apartmentgroceries.auth.LoginActivity;
import com.quarkworks.apartmentgroceries.main.HomeActivity;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        new BackgroundSplashTask().execute();
    }

    private class BackgroundSplashTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean requireLogin = false;
            SharedPreferences sharedPreferences = getApplication()
                    .getSharedPreferences(getApplication().getString(R.string.login_or_sign_up_session), 0);
            String sessionToken = sharedPreferences.getString("sessionToken", null);
            if (sessionToken == null || sessionToken.isEmpty()) return true;

            int SPLASH_SHOW_TIME = 3000;
            try {
                Thread.sleep(SPLASH_SHOW_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return requireLogin;
        }

        @Override
        protected void onPostExecute(Boolean requireLogin) {
            super.onPostExecute(requireLogin);
            Intent intent;
            if (requireLogin) {
                intent = new Intent(MainActivity.this, LoginActivity.class);
            } else {
                intent = new Intent(MainActivity.this, HomeActivity.class);
            }
            startActivity(intent);
            finish();
        }

    }
}
