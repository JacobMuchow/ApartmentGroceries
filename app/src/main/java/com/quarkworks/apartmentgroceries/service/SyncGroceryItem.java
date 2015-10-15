package com.quarkworks.apartmentgroceries.service;

import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.service.models.Grocery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Created by zz on 10/14/15.
 */
public class SyncGroceryItem {
    private static final String TAG = SyncGroceryItem.class.getSimpleName();

    private static final class JsonKeys {
        private static final String RESULTS = "results";
        private static final String NAME = "name";
    }

    public static Promise getAll() {

        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {

                Log.d(TAG, "grocery item jsonObject:" + jsonObject.toString());

                Realm realm = DataStore.getDataStore().getRealm();
                realm.beginTransaction();
                realm.clear(Grocery.class);

                JSONArray groceryJsonArray = jsonObject.optJSONArray(JsonKeys.RESULTS);

                for (int i = 0; i < groceryJsonArray.length(); i++) {

                    try {
                        Grocery grocery = realm.createObject(Grocery.class);
                        grocery.setName(groceryJsonArray.getJSONObject(i).optString(JsonKeys.NAME));

                    } catch(JSONException e) {
                        Log.d(TAG, "Error parsing grocery object");
                    }

                }

                realm.commitTransaction();

                promise.onSuccess();

                //or promise.onFailure() depending
            }
        };

        UrlTemplate template = UrlTemplateCreator.getAllGroceryItem();
        new NetworkRequest(template, callback).execute();
        return promise;
    }
}
