package com.quarkworks.apartmentgroceries.group;

import android.content.Context;
import android.content.Intent;
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
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.SyncGroup;
import com.quarkworks.apartmentgroceries.service.models.RGroup;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class GroupActivity extends AppCompatActivity {
    private static final String TAG = GroupActivity.class.getSimpleName();

    /*
        References
     */
    private Toolbar toolbar;
    private TextView titleTextView;
    public static RealmBaseAdapter<RGroup> groupRealmBaseAdapter;

    public static void newIntent(Context context) {
        Intent intent = new Intent(context, GroupActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity);

        /**
         * Get view references
         */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar_id);
        titleTextView = (TextView) toolbar.findViewById(R.id.toolbar_title_id);

        /**
         * Set view data
         */
        titleTextView.setText(getString(R.string.title_activity_group));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        SyncGroup.getAll();

        final RealmResults<RGroup> groups = DataStore.getInstance().getRealm().where(RGroup.class).findAll();

        groupRealmBaseAdapter = new RealmBaseAdapter<RGroup>(this, groups, true) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                GroupCell groupCell = convertView != null ?
                        (GroupCell) convertView : new GroupCell(parent.getContext());
                groupCell.setViewData(getItem(position));
                return groupCell;
            }
        };

        ListView listView = (ListView) findViewById(R.id.group_list_view_id);
        listView.setAdapter(groupRealmBaseAdapter);
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
