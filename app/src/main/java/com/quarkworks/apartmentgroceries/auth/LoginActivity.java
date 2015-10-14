package com.quarkworks.apartmentgroceries.auth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.NetworkRequest;
import com.squareup.okhttp.Request;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView statusTextView;

    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        usernameEditText = (EditText) findViewById(R.id.login_activity_username_id);
        passwordEditText = (EditText) findViewById(R.id.login_activity_password_id);
        statusTextView = (TextView) findViewById(R.id.login_activity_status_id);
    }

    /**
     * login button onclick
     * @param view
     */
    public void loginButtonOnClick(View view) {

        username = usernameEditText.getText().toString();
        password = passwordEditText.getText().toString();

        Log.d(LOG_TAG, "username:" + username);

        if (username != null && password != null) {
            login(username, password);

        } else {
            statusTextView.setText("please input username and password");
        }
    }

    /**
     * login AsyncTask
     * input: username and password
     * TODO: use SyncUser.login();
     */
    private void login(String username, String password) {

        if (username == null || password == null) return;

        String url = "https://api.parse.com/1/login?username=" + username
                + "&password=" + password;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Parse-Application-Id", "GlvuJjSGKTkc3DxedowpvgCMNOZeGQjxvRApSqGD")
                .addHeader("X-Parse-REST-API-Key", "0b3HDSEgt3EgXxyDHLOV0M7yQZwsexVG8ryTqzKI")
                .addHeader("X-Parse-Revocable-Session", "1")
                .method("GET", null) // GET method not allow request body so we pass username and secret in url directly
                .build();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {

            @Override
            public void done(JSONObject jsonObject) {
                if (jsonObject == null) {
                    Log.e(LOG_TAG, "Error get login response json");
                    return;
                }

                saveLoginSessionInfo(jsonObject);
            }

            private void saveLoginSessionInfo(JSONObject jsonObject) {

                String sessionToken = jsonObject.optString("sessionToken");
                String username = jsonObject.optString("username");

                SharedPreferences sharedPreferences = getSharedPreferences("login", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("sessionToken", sessionToken);
                editor.putString("username", username);
                editor.commit();
            }
        };

        //new NetworkRequest(request, callback).execute();
    }
}
