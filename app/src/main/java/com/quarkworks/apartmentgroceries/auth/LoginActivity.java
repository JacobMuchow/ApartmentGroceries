package com.quarkworks.apartmentgroceries.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.group.GroupActivity;
import com.quarkworks.apartmentgroceries.main.HomeActivity;
import com.quarkworks.apartmentgroceries.service.Promise;
import com.quarkworks.apartmentgroceries.service.SyncUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    /*
        References
     */
    private Toolbar toolbar;
    private TextView titleTextView;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signUpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        /**
         * Get view references
         */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar_id);
        titleTextView = (TextView) toolbar.findViewById(R.id.toolbar_title_id);
        usernameEditText = (EditText) findViewById(R.id.login_activity_username_id);
        passwordEditText = (EditText) findViewById(R.id.login_activity_password_id);
        loginButton = (Button) findViewById(R.id.login_activity_login_button_id);
        signUpTextView = (TextView) findViewById(R.id.login_activity_sign_up_id);

        /**
         * Set view data
         */
        titleTextView.setText(getString(R.string.title_activity_login));

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    SyncUser.login(username, password)
                            .setCallbacks(loginSuccessCallback, loginFailureCallback);
                } else {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.empty_username_or_password),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    public Promise.Success loginSuccessCallback = new Promise.Success() {
        @Override
        public void onSuccess() {
            SharedPreferences sharedPreferences = getApplication()
                    .getSharedPreferences(getString(R.string.login_or_sign_up_session), 0);
            String groupId = sharedPreferences.getString(SyncUser.JsonKeys.GROUP_ID, null);
            Intent intent;
            if (TextUtils.isEmpty(groupId)) {
                intent = new Intent(MyApplication.getContext(), GroupActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), getString(R.string.login_success_message),
                        Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), getString(R.string.choose_group_message),
                        Toast.LENGTH_SHORT).show();
            } else {
                intent = new Intent(MyApplication.getContext(), HomeActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), getString(R.string.login_success_message),
                        Toast.LENGTH_SHORT).show();
            }

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
