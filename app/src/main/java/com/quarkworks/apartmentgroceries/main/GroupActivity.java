package com.quarkworks.apartmentgroceries.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.SyncGroceryItem;
import com.quarkworks.apartmentgroceries.service.SyncGroup;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.RGroup;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class GroupActivity extends AppCompatActivity {
    private static final String TAG = GroupActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity);

        SyncGroup.getAll();

        final RealmResults<RGroup> groups = DataStore.getInstance().getRealm().where(RGroup.class).findAll();

        RealmBaseAdapter<RGroup> realmBaseAdapter = new RealmBaseAdapter<RGroup>(this, groups, true) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                GroupCell groupCell = convertView != null ?
                        (GroupCell) convertView : new GroupCell(parent.getContext());
                groupCell.setViewData(getItem(position));
                return groupCell;
            }
        };

        ListView listView = (ListView) findViewById(R.id.group_list_view_id);
        listView.setAdapter(realmBaseAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.action_add_group:
                intent = new Intent(this, AddGroupActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
