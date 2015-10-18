package com.quarkworks.apartmentgroceries.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
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
        private static final String GROUPID = "groupId";
        private static final String NAME = "name";
        private static final String OBJECTID = "objectId";
        private static final String RESULTS = "results";
        private static final String CREATEDBY = "createdBy";
        private static final String PURCHASEDBY = "purchasedBy";
        private static final String USERNAME = "username";
    }

    public static Promise getAll() {

        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {

                Log.d(TAG, "grocey item jsonObject:" + jsonObject.toString());

                Realm realm = DataStore.getInstance().getRealm();
                realm.beginTransaction();
                realm.clear(RGroceryItem.class);

                try {

                    JSONArray groceryJsonArray = jsonObject.optJSONArray(JsonKeys.RESULTS);
                    if (groceryJsonArray != null) {
                        for (int i = 0; i < groceryJsonArray.length(); i++) {
                            RGroceryItem groceryItem = realm.createObject(RGroceryItem.class);
                            groceryItem.setGroceryId(groceryJsonArray.getJSONObject(i)
                                    .getString(JsonKeys.OBJECTID));
                            groceryItem.setName(groceryJsonArray.getJSONObject(i)
                                    .getString(JsonKeys.NAME));
                            groceryItem.setGroupId(groceryJsonArray.getJSONObject(i)
                                    .getJSONObject(JsonKeys.GROUPID).getString(JsonKeys.OBJECTID));
                            groceryItem.setGroupName(groceryJsonArray.getJSONObject(i)
                                    .getJSONObject(JsonKeys.GROUPID).getString(JsonKeys.NAME));
                            groceryItem.setCreatedBy(groceryJsonArray.getJSONObject(i)
                                    .getJSONObject(JsonKeys.CREATEDBY).getString(JsonKeys.USERNAME));
                            JSONObject purchasedByObj = groceryJsonArray.getJSONObject(i)
                                    .optJSONObject(JsonKeys.PURCHASEDBY);
                            if (purchasedByObj != null) {
                                groceryItem.setPurchasedBy(purchasedByObj.getString(JsonKeys.USERNAME));
                            }
                        }

                        realm.commitTransaction();
                    } else {
                        realm.cancelTransaction();
                    }
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

    public static Promise add(RGroceryItem rGroceryItem) {

        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {
                Log.d(TAG, "add grocery jsonObject:" + jsonObject);

                try {
                    String groceryId = jsonObject.getString(JsonKeys.OBJECTID);
                    if (!groceryId.isEmpty()) {
                        promise.onSuccess();
                    } else {
                        promise.onFailure();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "adding grocery failed", e);
                    promise.onFailure();
                }
            }
        };

        UrlTemplate template = UrlTemplateCreator.addGroceryItem(rGroceryItem);
        new NetworkRequest(template, callback).execute();
        return promise;
    }
}
