package com.quarkworks.apartmentgroceries.service;

import android.support.annotation.Nullable;
import android.util.Log;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
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
        private static final String CREATED_AT ="createdAt";
    }

    public static Task<Void> getAll() {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.getAllGroceryItems();
        NetworkRequestBolts networkRequestBolts = new NetworkRequestBolts(template, taskCompletionSource);

        return networkRequestBolts.runNetworkRequestBolts().onSuccess(new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                JSONObject jsonObject = task.getResult();

                if (jsonObject == null) {
                    Log.e(TAG, "Error getting grocery items from server");
                    return null;
                }

                Realm realm = Realm.getInstance(MyApplication.getContext());
                realm.beginTransaction();
                realm.clear(RGroceryItem.class);
                Log.d(TAG, jsonObject.toString());
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
                                    .getJSONObject(JsonKeys.CREATED_BY).getString(JsonKeys.OBJECT_ID));
                            groceryItem.setCreatedAt(groceryJsonObj.getString(JsonKeys.CREATED_AT));
                            JSONObject purchasedByObj = groceryJsonObj.optJSONObject(JsonKeys.PURCHASED_BY);
                            if (purchasedByObj != null) {
                                groceryItem.setPurchasedBy(purchasedByObj.getString(JsonKeys.USERNAME));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing grocery object", e);
                        }
                    }

                    realm.commitTransaction();
                } catch(JSONException e) {
                    Log.e(TAG, "Error getting grocery object from server", e);
                    realm.cancelTransaction();
                }
                return null;
            }
        });
    }

    public static Task<Boolean> add(RGroceryItem rGroceryItem) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.addGroceryItem(rGroceryItem);
        NetworkRequestBolts networkRequestBolts = new NetworkRequestBolts(template, taskCompletionSource);

        return networkRequestBolts.runNetworkRequestBolts().continueWith(new Continuation<JSONObject, Boolean>() {
            @Override
            public Boolean then(Task<JSONObject> task) throws Exception {
                JSONObject jsonObject = task.getResult();

                if (jsonObject == null) {
                    Log.e(TAG, "Error adding grocery object");
                    return false;
                }

                try {
                    String groceryId = jsonObject.getString(JsonKeys.OBJECT_ID);
                    if (!groceryId.isEmpty()) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "adding grocery failed", e);
                    return false;
                }
            }
        });
    }
}
