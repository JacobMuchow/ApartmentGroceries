package com.quarkworks.apartmentgroceries.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.main.HomeActivity;
import com.quarkworks.apartmentgroceries.service.Promise;
import com.quarkworks.apartmentgroceries.service.SyncUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    /*
        References
     */
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        usernameEditText = (EditText) findViewById(R.id.login_activity_username_id);
        passwordEditText = (EditText) findViewById(R.id.login_activity_password_id);
        loginButton = (Button) findViewById(R.id.login_activity_login_button_id);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    SyncUser.login(username, password)
                            .setCallbacks(loginSuccesCallback, loginFailureCallback);
                } else {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.empty_username_or_password),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private Promise.Success loginSuccesCallback = new Promise.Success() {
        @Override
        public void onSuccess() {
            Intent intent = new Intent(MyApplication.getContext(), HomeActivity.class);
            startActivity(intent);
            Toast.makeText(getApplicationContext(), getString(R.string.login_success_message),
                    Toast.LENGTH_LONG).show();
        }
    };

    private Promise.Failure loginFailureCallback = new Promise.Failure() {
        @Override
        public void onFailure() {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.login_failure_message), Toast.LENGTH_LONG).show();
        }
    };
}
