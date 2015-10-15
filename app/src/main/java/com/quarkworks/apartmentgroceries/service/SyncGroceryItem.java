package com.quarkworks.apartmentgroceries.service;

import android.support.annotation.Nullable;
import android.util.Log;

import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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


                Realm realm = DataStore.getInstance().getRealm();
                realm.beginTransaction();
                realm.clear(RGroceryItem.class);

                try {

                    JSONArray groceryJsonArray = jsonObject.optJSONArray(JsonKeys.RESULTS);

                    for (int i = 0; i < groceryJsonArray.length(); i++) {
                        RGroceryItem groceryItem = realm.createObject(RGroceryItem.class);
                        groceryItem.setName(groceryJsonArray.getJSONObject(i).optString(JsonKeys.NAME));
                    }

                    realm.commitTransaction();
                    promise.onSuccess();
                } catch(JSONException e) {
                    Log.e(TAG, "Error parsing grocery object", e);
                    realm.cancelTransaction();
                    promise.onFailure();
                }
            }
        };

        UrlTemplate template = UrlTemplateCreator.getAllGroceryItems();
        new NetworkRequest(template, callback).execute();
        return promise;
    }
}
