package com.quarkworks.apartmentgroceries.auth;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.quarkworks.apartmentgroceries.service.SyncUser;

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

    public static void newIntent(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }

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
                        final Continuation<Void, Object> loginOnSuccess = new Continuation<Void, Object>() {
                            @Override
                            public Void then(Task<Void> task) {
                                if (task.isFaulted()) {
                                    Log.e(TAG, task.getError().toString());
                                } else {
                                    progressBar.setVisibility(View.GONE);

                                    GroupActivity.newIntent(SignUpActivity.this);
                                    Toast.makeText(getApplicationContext(), getString(R.string.login_success_message),
                                            Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getApplicationContext(), getString(R.string.choose_group_message),
                                            Toast.LENGTH_SHORT).show();
                                }
                                return null;
                            }
                        };

                        Continuation<Void, Void> signUpOnSuccess = new Continuation<Void, Void>() {
                            @Override
                            public Void then(Task<Void> task) {
                                if (task.isFaulted()) {
                                    Log.e(TAG, task.getError().toString());
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(MyApplication.getContext(),
                                            getString(R.string.sign_up_failure_message), Toast.LENGTH_SHORT).show();
                                } else {
                                    SyncUser.loginBolts(username, password).onSuccess(loginOnSuccess, Task.UI_THREAD_EXECUTOR);
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
