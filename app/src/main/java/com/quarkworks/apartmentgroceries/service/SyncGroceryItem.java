package com.quarkworks.apartmentgroceries.service;

import android.text.TextUtils;
import android.util.Log;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.realm.Realm;

import com.quarkworks.apartmentgroceries.service.models.RGroceryItem.JsonKeys;

/**
 * Created by zz on 10/14/15.
 */
public class SyncGroceryItem {
    private static final String TAG = SyncGroceryItem.class.getSimpleName();

    public static Task<Void> getAll() {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.getAllGroceryItems();
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Void> addGroceryItemsToRealm = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {

                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in getAll", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();

                if (jsonObject == null) {
                    throw new InvalidResponseException("Empty response");
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
                                groceryItem.setPurchasedBy(purchasedByObj.getString(JsonKeys.OBJECT_ID));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing grocery object", e);
                        }
                    }

                    realm.commitTransaction();
                } catch (JSONException e) {
                    realm.cancelTransaction();
                    throw new InvalidResponseException("Error getting grocery object from server");
                }
                realm.close();

                return null;
            }
        };

        return networkRequest.runNetworkRequest().onSuccess(addGroceryItemsToRealm);
    }

    public static Task<Void> add(RGroceryItem rGroceryItem) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.addGroceryItem(rGroceryItem);
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Void> continuation = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in add", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();

                if (jsonObject == null) {
                    throw new InvalidResponseException("Empty response");
                }

                try {
                    String groceryId = jsonObject.getString(JsonKeys.OBJECT_ID);
                    if (TextUtils.isEmpty(groceryId)) {
                        throw new InvalidResponseException("Incorrect response");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing grocery object", e);
                }

                return null;
            }
        };

        return networkRequest.runNetworkRequest().continueWith(continuation);
    }
}
