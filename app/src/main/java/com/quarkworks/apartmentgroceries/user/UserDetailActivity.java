package com.quarkworks.apartmentgroceries.user;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.grocery.GroceryCell;

public class UserDetailActivity extends AppCompatActivity {
    private static final String TAG = UserDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_detail_activity);

        String username = getIntent().getExtras().getString(GroceryCell.USERNAME);
        Toast.makeText(this, "username:" + username, Toast.LENGTH_SHORT).show();
    }
}
