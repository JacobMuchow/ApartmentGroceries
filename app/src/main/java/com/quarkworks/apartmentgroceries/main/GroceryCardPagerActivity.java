package com.quarkworks.apartmentgroceries.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.quarkworks.apartmentgroceries.R;

public class GroceryCardPagerActivity extends AppCompatActivity {
    private static final String TAG = GroceryCardPagerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grocery_card_pager_activity);

        int position = getIntent().getExtras().getInt(HomeActivity.POSITION);
        Log.d(TAG, "position:" + position);
    }
}
