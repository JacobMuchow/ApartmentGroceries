package com.quarkworks.apartmentgroceries.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.group.GroupActivity;
import com.quarkworks.apartmentgroceries.main.HomeActivity;
import com.quarkworks.apartmentgroceries.service.Promise;
import com.quarkworks.apartmentgroceries.service.SyncUser;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    private String username;
    private String password;
    /*
        References
     */
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText secondPasswordEditText;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);

        usernameEditText = (EditText) findViewById(R.id.sign_up_username_id);
        passwordEditText = (EditText) findViewById(R.id.sign_up_password_id);
        secondPasswordEditText = (EditText) findViewById(R.id.sign_up_second_password_id);
        signUpButton = (Button) findViewById(R.id.sign_up_submit_button_id);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();
                String secondPassword = secondPasswordEditText.getText().toString();

                if (!username.isEmpty() && !password.isEmpty() && !secondPassword.isEmpty()) {
                    if (password.equals(secondPassword)) {
                        SyncUser.signUp(username, password)
                                .setCallbacks(signUpSuccessCallback, signUpFailureCallback);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.password_not_match),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.empty_username_or_password),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private Promise.Success signUpSuccessCallback = new Promise.Success() {
        @Override
        public void onSuccess() {
            SyncUser.login(username, password).setSuccessCallack(signUpLoginSuccesCallback);
        }
    };

    private Promise.Failure signUpFailureCallback = new Promise.Failure() {
        @Override
        public void onFailure() {
            Toast.makeText(MyApplication.getContext(),
                    getString(R.string.sign_up_failure_message), Toast.LENGTH_SHORT).show();
        }
    };

    private Promise.Success signUpLoginSuccesCallback = new Promise.Success() {
        @Override
        public void onSuccess() {
            SharedPreferences sharedPreferences = getApplication()
                    .getSharedPreferences(getApplication().getString(R.string.login_or_sign_up_session), 0);
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
}
