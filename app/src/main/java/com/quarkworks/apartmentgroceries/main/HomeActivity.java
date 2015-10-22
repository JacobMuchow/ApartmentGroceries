package com.quarkworks.apartmentgroceries.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.auth.LoginActivity;
import com.quarkworks.apartmentgroceries.grocery.AddGroceryItemActivity;
import com.quarkworks.apartmentgroceries.grocery.GroceryCell;
import com.quarkworks.apartmentgroceries.group.GroupActivity;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.SyncGroceryItem;
import com.quarkworks.apartmentgroceries.service.SyncUser;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.user.UserActivity;

import bolts.Continuation;
import bolts.Task;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    /*
        References
     */
    private Toolbar toolbar;
    private TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        /**
         * Get view references
         */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar_id);
        titleTextView = (TextView) toolbar.findViewById(R.id.toolbar_title_id);

        /**
         * Set view data
         */
        titleTextView.setText(getString(R.string.title_activity_home));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        SyncGroceryItem.getAll();

        final RealmResults<RGroceryItem> groceries = DataStore.getInstance().getRealm().where(RGroceryItem.class).findAll();

        RealmBaseAdapter<RGroceryItem> realmBaseAdapter = new RealmBaseAdapter<RGroceryItem>(this, groceries, true) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                GroceryCell groceryCell = convertView != null ?
                        (GroceryCell) convertView : new GroceryCell(parent.getContext());
                groceryCell.setViewData(getItem(position), position);
                return groceryCell;
            }
        };

        ListView listView = (ListView) findViewById(R.id.home_list_view_id);
        listView.setAdapter(realmBaseAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
            case R.id.action_add_grocery:
                intent = new Intent(this, AddGroceryItemActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_list_group:
                intent = new Intent(this, GroupActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_user:
                intent = new Intent(this, UserActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_logout:
                SyncUser.logout().continueWith(new Continuation<Boolean, Void>() {
                    @Override
                    public Void then(Task<Boolean> task) throws Exception {
                        SharedPreferences sharedPreferences =
                                getSharedPreferences(getString(R.string.login_or_sign_up_session), 0);
                        sharedPreferences.edit().clear().commit();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        return null;
                    }
                });
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
