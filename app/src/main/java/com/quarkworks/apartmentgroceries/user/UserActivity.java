package com.quarkworks.apartmentgroceries.user;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.SyncUser;
import com.quarkworks.apartmentgroceries.service.models.RUser;

import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = UserActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);

        SyncUser.getAll();

        RealmResults<RUser> users = DataStore.getInstance().getRealm().where(RUser.class).findAll();

        RealmBaseAdapter<RUser> realmBaseAdapter = new RealmBaseAdapter<RUser>(this, users, true) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                UserCell userCell = convertView != null ?
                        (UserCell) convertView : new UserCell(parent.getContext());
                userCell.setViewData(getItem(position));

                return userCell;
            }
        };

        ListView listView = (ListView) findViewById(R.id.user_list_view_id);
        listView.setAdapter(realmBaseAdapter);
    }
}
