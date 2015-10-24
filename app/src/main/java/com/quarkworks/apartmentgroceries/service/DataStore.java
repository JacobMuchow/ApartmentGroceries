package com.quarkworks.apartmentgroceries.service;

import com.quarkworks.apartmentgroceries.MyApplication;

import io.realm.Realm;

/**
 * Created by zz on 10/14/15.
 */
public class DataStore {
    private static final String TAG = DataStore.class.getSimpleName();

    private static DataStore dataStore;
    private Realm realm;

    private DataStore() {
        realm = Realm.getDefaultInstance();
    }

    public static DataStore getInstance(){
        if (dataStore == null) {
            dataStore = new DataStore();
        }
        return dataStore;
    }

    public Realm getRealm(){
        return realm;
    }
}
