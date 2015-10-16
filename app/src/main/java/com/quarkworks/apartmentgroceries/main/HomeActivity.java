package com.quarkworks.apartmentgroceries.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.SyncGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    public static final String POSITION = "POSITION";

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
                groceryCell.setViewData(getItem(position));
                Log.d(TAG, "groupId:" + getItem(position).getGroupId());
                return groceryCell;
            }
        };

        ListView listView = (ListView) findViewById(R.id.home_list_view_id);
        listView.setAdapter(realmBaseAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), GroceryCardPagerActivity.class);
                intent.putExtra(POSITION, position);
                startActivity(intent);
            }
        });
    }
}
