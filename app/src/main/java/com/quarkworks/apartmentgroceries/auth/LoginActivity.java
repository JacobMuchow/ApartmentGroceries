package com.quarkworks.apartmentgroceries.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.main.HomeActivity;
import com.quarkworks.apartmentgroceries.service.Promise;
import com.quarkworks.apartmentgroceries.service.SyncUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

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

        if (username != null && password != null) {
            SyncUser.login(username, password);
            loginSuccesCallback.onSuccess();

        } else {
            statusTextView.setText("please input username and password");
        }
    }

    /**
     * Remote sync callbacks
     */
    private Promise.Success loginSuccesCallback = new Promise.Success() {
        @Override
        public void onSuccess() {
            // Launch home activity
            Intent intent = new Intent(MyApplication.getContext(), HomeActivity.class);
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "login success", Toast.LENGTH_LONG).show();
        }
    };

    private Promise.Failure loginFailureCallback = new Promise.Failure() {
        @Override
        public void onFailure() {
            //TODO: failure message
        }
    };
}
