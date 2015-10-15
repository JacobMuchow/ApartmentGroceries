package com.quarkworks.apartmentgroceries.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.SyncGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.Grocery;

import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private RealmResults<Grocery> groceries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        SyncGroceryItem.getAll();

        Realm realm = Realm.getInstance(this);

        RealmQuery<Grocery> query = realm.where(Grocery.class);
        groceries = query.findAll();

        RealmBaseAdapter<Grocery> realmBaseAdapter = new RealmBaseAdapter<Grocery>(this, groceries, true) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                GroceryCell groceryCell = convertView != null ?
                        (GroceryCell) convertView : new GroceryCell(parent.getContext());
                groceryCell.setViewData(getItem(position));

                return groceryCell;
            }
        };

        ListView listView = (ListView) findViewById(R.id.home_list_view_id);
        listView.setAdapter(realmBaseAdapter);
    }
}
