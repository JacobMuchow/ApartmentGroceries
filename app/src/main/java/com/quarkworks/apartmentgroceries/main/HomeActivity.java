package com.quarkworks.apartmentgroceries.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

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
                SyncUser.logout();
                SharedPreferences sharedPreferences =
                        this.getSharedPreferences(getApplication().getString(R.string.login_or_sign_up_session), 0);
                sharedPreferences.edit().clear().commit();
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
