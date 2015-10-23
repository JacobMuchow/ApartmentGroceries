package com.quarkworks.apartmentgroceries.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.group.GroupActivity;
import com.quarkworks.apartmentgroceries.main.HomeActivity;
import com.quarkworks.apartmentgroceries.service.SyncUser;
import com.quarkworks.apartmentgroceries.service.models.RUser;

import bolts.Continuation;
import bolts.Task;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    private String username;
    private String password;

    /*
        References
     */
    private Toolbar toolbar;
    private TextView titleTextView;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signUpButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);

        /**
         * Get view references
         */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar_id);
        titleTextView = (TextView) toolbar.findViewById(R.id.toolbar_title_id);
        usernameEditText = (EditText) findViewById(R.id.sign_up_username_id);
        passwordEditText = (EditText) findViewById(R.id.sign_up_password_id);
        confirmPasswordEditText = (EditText) findViewById(R.id.sign_up_confirm_password_id);
        signUpButton = (Button) findViewById(R.id.sign_up_submit_button_id);
        progressBar = (ProgressBar) findViewById(R.id.sign_up_activity_progress_bar_id);

        /**
         * Set view data
         */
        titleTextView.setText(getString(R.string.title_activity_signup));

        /**
         * Set view OnClickListener
         */
        signUpButton.setOnClickListener(signUpOnClick());
    }

    private View.OnClickListener signUpOnClick() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();
                String secondPassword = confirmPasswordEditText.getText().toString();

                if (!username.isEmpty() && !password.isEmpty() && !secondPassword.isEmpty()) {
                    if (password.equals(secondPassword)) {
                        progressBar.setVisibility(View.VISIBLE);
                        final Continuation<Boolean, Object> loginOnSuccess = new Continuation<Boolean, Object>() {
                            @Override
                            public Void then(Task<Boolean> task) {
                                if (task.getResult()) {
                                    progressBar.setVisibility(View.GONE);
                                    SharedPreferences sharedPreferences = getApplication()
                                            .getSharedPreferences(getApplication().getString(R.string.login_or_sign_up_session), 0);
                                    String groupId = sharedPreferences.getString(RUser.JsonKeys.GROUP_ID, null);
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
                                } else {
                                    Log.e(TAG, "Error login in after sign up");
                                }
                                return null;
                            }
                        };

                        Continuation<Boolean, Void> signUpOnSuccess = new Continuation<Boolean, Void>() {
                            @Override
                            public Void then(Task<Boolean> task) {
                                if (task.getResult()) {
                                    SyncUser.loginBolts(username, password).onSuccess(loginOnSuccess, Task.UI_THREAD_EXECUTOR);
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(MyApplication.getContext(),
                                            getString(R.string.sign_up_failure_message), Toast.LENGTH_SHORT).show();
                                }
                                return null;
                            }
                        };

                        SyncUser.signUp(username, password).continueWith(signUpOnSuccess, Task.UI_THREAD_EXECUTOR);
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
        };
    }
}
