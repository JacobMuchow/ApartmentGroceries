package com.quarkworks.apartmentgroceries.service;

import android.text.TextUtils;
import android.util.Log;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.realm.Realm;

import com.quarkworks.apartmentgroceries.service.models.RGroceryItem.JsonKeys;
import com.quarkworks.apartmentgroceries.service.models.RGroceryPhoto;

import java.util.ArrayList;
import java.util.Date;

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

    public static Task<Void> add(RGroceryItem rGroceryItem, final ArrayList<byte[]> photoList) {

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
                    } else {
                        for (int i = 0; i < photoList.size(); i++) {
                            addGroceryPhoto(groceryId, photoList.get(i));
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing grocery object", e);
                }

                return null;
            }
        };

        return networkRequest.runNetworkRequest().continueWith(continuation);
    }

    public static Task<JSONObject> addGroceryPhoto(final String groceryId, byte[] data) {


        final Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();

        Continuation<JSONObject, Void> addPhotoNameToGroceryPhoto = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in uploadPhoto", exception);
                    throw exception;
                }

                String photoName = task.getResult().optString("name");
                if (TextUtils.isEmpty(photoName)) {
                    throw new InvalidResponseException("Empty response");
                }

                final UrlTemplate template = UrlTemplateCreator.addGroceryPhoto(groceryId, photoName);

                Continuation<JSONObject, Void> addingGroceryPhoto = new Continuation<JSONObject, Void>() {
                    @Override
                    public Void then(Task<JSONObject> task) throws Exception{
                        if (task.isFaulted()) {
                            Exception exception = task.getError();
                            Log.e(TAG, "Error in addingGroceryPhoto", exception);
                            throw exception;
                        }

                        try {
                            Log.d(TAG, "adding to GroceryPhoto:" + task.getResult().toString());
                            String createdAt = task.getResult().getString("createdAt");
                            if (TextUtils.isEmpty(createdAt)) {
                                throw new InvalidResponseException("Incorrect adding to GroceryPhoto response");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing adding grocery photo response", e);
                        }
                        return null;
                    }
                };
                new NetworkRequest(template, taskCompletionSource).runNetworkRequest().continueWith(addingGroceryPhoto);

                return null;
            }
        };

        String photoName = Utilities.dateToString(new Date(), MyApplication.getContext()
                .getString(R.string.photo_date_format_string)) + ".jpg";
        SyncPhoto.uploadPhoto(photoName, data).continueWith(addPhotoNameToGroceryPhoto);

        return taskCompletionSource.getTask();
    }

    public static void getAllGroceryPhotos() {
        getAllGroceryPhotos(null);
    }

    public static Task<Void> getAllGroceryPhotos(String groceryId) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template;
        if (TextUtils.isEmpty(groceryId)) {
            template = UrlTemplateCreator.getAllGroceryPhotos();
        } else {
            template = UrlTemplateCreator.getGroceryPhotoByGroceryId(groceryId);
        }

        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Void> addGroceryPhotoToRealm = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in getAllGroceryPhotos", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();

                if (jsonObject == null) {
                    throw new InvalidResponseException("Empty response");
                }

                Realm realm = Realm.getInstance(MyApplication.getContext());
                realm.beginTransaction();

                try {
                    JSONArray groceryPhotoJsonArray = jsonObject.getJSONArray(JsonKeys.RESULTS);

                    for (int i = 0; i < groceryPhotoJsonArray.length(); i++) {
                        try {
                            RGroceryPhoto groceryPhoto = realm.createObject(RGroceryPhoto.class);
                            JSONObject groceryPhotoJsonObj = groceryPhotoJsonArray.getJSONObject(i);

                            groceryPhoto.setGroceryPhotoId(groceryPhotoJsonObj.getString(RGroceryPhoto.JsonKeys.OBJECT_ID));
                            groceryPhoto.setGroceryId(groceryPhotoJsonObj.getJSONObject(RGroceryPhoto.JsonKeys.GROCERY_ID).getString(JsonKeys.OBJECT_ID));
                            groceryPhoto.setUrl(groceryPhotoJsonObj.getJSONObject(RGroceryPhoto.JsonKeys.PHOTO).getString(RGroceryPhoto.JsonKeys.URL));

                            realm.copyToRealmOrUpdate(groceryPhoto);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing grocery photo object", e);
                        }
                    }
                    realm.commitTransaction();
                } catch (JSONException e) {
                    realm.cancelTransaction();
                    throw new InvalidResponseException("Error getting grocery photo object from server");
                }
                realm.close();

                return null;
            }
        };

        return networkRequest.runNetworkRequest().onSuccess(addGroceryPhotoToRealm);
    }
}
