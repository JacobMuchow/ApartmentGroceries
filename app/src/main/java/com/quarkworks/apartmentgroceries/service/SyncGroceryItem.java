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
        private static final String GROUP_ID = "groupId";
        private static final String NAME = "name";
        private static final String OBJECT_ID = "objectId";
        private static final String RESULTS = "results";
        private static final String CREATED_BY = "createdBy";
        private static final String PURCHASED_BY = "purchasedBy";
        private static final String USERNAME = "username";
    }

    public static Promise getAll() {

        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {
                if (jsonObject == null) {
                    Log.e(TAG, "Error getting grocery items from server");
                    promise.onFailure();
                    return;
                }

                Realm realm = DataStore.getInstance().getRealm();
                realm.beginTransaction();
                realm.clear(RGroceryItem.class);

                try {
                    JSONArray groceryJsonArray = jsonObject.getJSONArray(JsonKeys.RESULTS);

                    for (int i = 0; i < groceryJsonArray.length(); i++) {
                        try {
                            RGroceryItem groceryItem = realm.createObject(RGroceryItem.class);
                            JSONObject groceryJsonObj = groceryJsonArray.getJSONObject(i);

                            groceryItem.setGroceryId(groceryJsonObj.getString(JsonKeys.OBJECT_ID));
                            groceryItem.setName(groceryJsonObj.getString(JsonKeys.NAME));
                            groceryItem.setGroupId(groceryJsonObj
                                    .getJSONObject(JsonKeys.GROUP_ID).getString(JsonKeys.OBJECT_ID));
                            groceryItem.setGroupName(groceryJsonObj
                                    .getJSONObject(JsonKeys.GROUP_ID).getString(JsonKeys.NAME));
                            groceryItem.setCreatedBy(groceryJsonObj
                                    .getJSONObject(JsonKeys.CREATED_BY).getString(JsonKeys.USERNAME));
                            JSONObject purchasedByObj = groceryJsonObj.optJSONObject(JsonKeys.PURCHASED_BY);
                            if (purchasedByObj != null) {
                                groceryItem.setPurchasedBy(purchasedByObj.getString(JsonKeys.USERNAME));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing grocery object", e);
                        }
                    }

                    realm.commitTransaction();
                    promise.onSuccess();
                } catch(JSONException e) {
                    Log.e(TAG, "Error getting grocery object from server", e);
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
                if (jsonObject == null) {
                    Log.e(TAG, "Error adding grocery object");
                    promise.onFailure();
                    return;
                }

                try {
                    String groceryId = jsonObject.getString(JsonKeys.OBJECT_ID);
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
