package com.quarkworks.apartmentgroceries.user;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.R;

public class UserDetailActivity extends AppCompatActivity {
    private static final String TAG = UserDetailActivity.class.getSimpleName();

    private static final String USER_ID = "userId";

    public static void newIntent(Context context, String groceryId) {
        Intent intent = new Intent(context, UserDetailActivity.class);
        intent.putExtra(USER_ID, groceryId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_detail_activity);

        String username = getIntent().getStringExtra(USER_ID);
        Toast.makeText(this, "username:" + username, Toast.LENGTH_SHORT).show();
    }
}
