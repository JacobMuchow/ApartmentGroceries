package com.quarkworks.apartmentgroceries.auth;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.R;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class SignupActivity extends AppCompatActivity {

    private static final String LOG_TAG = SignupActivity.class.getSimpleName();

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText phoneEditText;

    private String username;
    private String password;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        usernameEditText = (EditText) findViewById(R.id.signup_activity_username_id);
        passwordEditText = (EditText) findViewById(R.id.signup_activity_password_id);
        phoneEditText = (EditText) findViewById(R.id.signup_activity_phone_id);
        
    }

    /**
     * sign up button onclick
     * @param view
     */
    public void signupButtonOnClick(View view) {

        username = usernameEditText.getText().toString();
        password = passwordEditText.getText().toString();
        phone = phoneEditText.getText().toString();
        Log.d(LOG_TAG, "username:" + username + ", password:" + password + ", phone:" + phone);

        if (username != null && password != null) {
            SignupAsyncTask signupAsyncTask = new SignupAsyncTask();
            signupAsyncTask.execute(username, password);
        } else {
            Toast.makeText(this, "sorry, please input username and password.", Toast.LENGTH_LONG).show();
        }


    }

    private class SignupAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String username = params[0];
            String password = params[1];
//            String phone = params[2];

            String json = "{\"username\":\"" +
                    username +
                    "\", \"password\":\"" +
                    password +
//                    "\", \"phone\":\"" +
//                    phone +
                    "\"}";
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, json);
            Log.d(LOG_TAG, "sign up request body:" + requestBody.toString());
            OkHttpClient client;
            client = new OkHttpClient();

            String url = "https://api.parse.com/1/users";
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-Parse-Application-Id", "GlvuJjSGKTkc3DxedowpvgCMNOZeGQjxvRApSqGD")
                    .addHeader("X-Parse-REST-API-Key", "0b3HDSEgt3EgXxyDHLOV0M7yQZwsexVG8ryTqzKI")
                    .addHeader("X-Parse-Revocable-Session", "1")
                    .method("POST", requestBody)
                    .build();

            Response response = null;


            try {
                response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error: client.newCall failed");
            }

            return null;
        }

        protected void onPostExecute(String result) {
            //todo: set up profile SharedPreferences
        }
    }
}
