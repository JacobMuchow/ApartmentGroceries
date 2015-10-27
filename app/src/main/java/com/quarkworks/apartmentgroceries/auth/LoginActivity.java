package com.quarkworks.apartmentgroceries.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.group.GroupActivity;
import com.quarkworks.apartmentgroceries.main.HomeActivity;
import com.quarkworks.apartmentgroceries.service.SyncUser;
import com.quarkworks.apartmentgroceries.service.models.RUser;

import bolts.Continuation;
import bolts.Task;

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
    private ProgressBar progressBar;

    public static void newIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

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
        progressBar = (ProgressBar) findViewById(R.id.login_activity_progress_bar_id);

        /**
         * Set view data
         */
        titleTextView.setText(getString(R.string.title_activity_login));

        /**
         * Set view on click
         */
        loginButton.setOnClickListener(loginButtonOnClick());
        signUpTextView.setOnClickListener(signUpOnClick());
    }

    public View.OnClickListener loginButtonOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    progressBar.setVisibility(View.VISIBLE);

                    Continuation<Void, Void> loginOnSuccess = new Continuation<Void, Void>() {
                        @Override
                        public Void then(Task<Void> task) throws Exception {
                            if (task.isFaulted()) {
                                Log.e(TAG, task.getError().toString());
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.login_failure_message), Toast.LENGTH_LONG).show();
                                return null;
                            }

                            SharedPreferences sharedPreferences = getApplication()
                                    .getSharedPreferences(getString(R.string.login_or_sign_up_session), 0);
                            String groupId = sharedPreferences.getString(RUser.JsonKeys.GROUP_ID, null);

                            if (TextUtils.isEmpty(groupId)) {
                                GroupActivity.newIntent(LoginActivity.this);
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), getString(R.string.login_success_message),
                                        Toast.LENGTH_SHORT).show();
                                Toast.makeText(getApplicationContext(), getString(R.string.choose_group_message),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                HomeActivity.newIntent(LoginActivity.this);
                                Toast.makeText(getApplicationContext(), getString(R.string.login_success_message),
                                        Toast.LENGTH_SHORT).show();
                            }

                            return null;
                        }
                    };

                    SyncUser.login(username, password).continueWith(loginOnSuccess, Task.UI_THREAD_EXECUTOR);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.empty_username_or_password),
                            Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    public View.OnClickListener signUpOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpActivity.newIntent(LoginActivity.this);
            }
        };
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
