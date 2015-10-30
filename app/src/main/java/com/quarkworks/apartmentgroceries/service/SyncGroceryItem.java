package com.quarkworks.apartmentgroceries.service;

import android.content.SharedPreferences;
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
import com.quarkworks.apartmentgroceries.service.models.RUser;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by zz on 10/14/15.
 */
public class SyncGroceryItem {
    private static final String TAG = SyncGroceryItem.class.getSimpleName();

    public static Task<Void> getAll(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            Log.e(TAG, "groupId is null");
            return null;
        }

        Task<JSONObject>.TaskCompletionSource tcs = Task.create();
        UrlTemplate template = UrlTemplateCreator.getAllGroceryItemsByGroupId(groupId);
        NetworkRequest networkRequest = new NetworkRequest(template, tcs);

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

    public static Task<Void> add(final GroceryItemBuilder builder) {
        SharedPreferences sharedPreferences = MyApplication.getContext()
                .getSharedPreferences(MyApplication.getContext()
                        .getString(R.string.login_or_sign_up_session), 0);
        final String groupId = sharedPreferences.getString(RUser.JsonKeys.GROUP_ID, null);
        String userId = sharedPreferences.getString(RUser.JsonKeys.USER_ID, null);

        if(TextUtils.isEmpty(groupId) || TextUtils.isEmpty(userId)) {
            Log.e(TAG, "groupId or userId is null");
            return null;
        }

        builder.setGroupId(groupId);
        builder.setCreatedBy(userId);

        Task<JSONObject>.TaskCompletionSource tcs = Task.create();
        UrlTemplate template = UrlTemplateCreator.addGroceryItem(builder);
        NetworkRequest networkRequest = new NetworkRequest(template, tcs);

        Continuation<JSONObject, Void> addGroceryItem = new Continuation<JSONObject, Void>() {
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
                        for (int i = 0; i < builder.getPhotoList().size(); i++) {
                            addGroceryPhoto(groceryId, builder.getPhotoList().get(i));
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing grocery object", e);
                }

                return null;
            }
        };

        return networkRequest.runNetworkRequest().continueWith(addGroceryItem);
    }

    public static Task<Void> addGroceryPhoto(final String groceryId, byte[] data) {
        SharedPreferences sharedPreferences = MyApplication.getContext()
                .getSharedPreferences(MyApplication.getContext()
                        .getString(R.string.login_or_sign_up_session), 0);
        final String groupId = sharedPreferences.getString(RUser.JsonKeys.GROUP_ID, null);

        if(TextUtils.isEmpty(groupId)) {
            Log.e(TAG, "groupId is null");
            return null;
        }

        Continuation<JSONObject, Task<JSONObject>> addGroceryPhotoItem = new Continuation<JSONObject, Task<JSONObject>>() {
            @Override
            public Task<JSONObject> then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in addGroceryPhotoItem", exception);
                    throw exception;
                }

                String photoName = task.getResult().optString("name");
                if (TextUtils.isEmpty(photoName)) {
                    throw new InvalidResponseException("Empty input for addGroceryPhotoItem");
                }

                Log.d(TAG, "addGroceryPhotoItem - photoName:" + photoName);
                Task<JSONObject>.TaskCompletionSource tcs = Task.create();
                UrlTemplate template = UrlTemplateCreator.addGroceryPhoto(groceryId, photoName, groupId);
                return new NetworkRequest(template, tcs).runNetworkRequest();

            }
        };

        Continuation<JSONObject, Task<JSONObject>> getGroceryPhotoById = new Continuation<JSONObject, Task<JSONObject>>() {
            @Override
            public Task<JSONObject> then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in getGroceryPhotoById", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();
                if (jsonObject == null) {
                    throw new InvalidResponseException("Empty input for getGroceryPhotoById");
                }

                Log.d(TAG, "getGroceryPhotoById:" + jsonObject.toString());
                String groceryPhotoId = jsonObject.getString("objectId");
                UrlTemplate template = UrlTemplateCreator.getGroceryPhotoByGroceryPhotoId(groceryPhotoId);
                Task<JSONObject>.TaskCompletionSource tcs = Task.create();

                return new NetworkRequest(template, tcs).runNetworkRequest();
            }
        };

        Continuation<JSONObject, Void> addGroceryPhotoToRealm = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in addGroceryPhotoToRealm", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();
                if (jsonObject == null) {
                    throw new InvalidResponseException("Empty input for addGroceryPhotoToRealm");
                }

                String groceryPhotoId = jsonObject.getString(RGroceryPhoto.JsonKeys.OBJECT_ID);
                String groceryId = jsonObject.getJSONObject(RGroceryPhoto.JsonKeys.GROCERY_ID)
                        .getString(RGroceryPhoto.JsonKeys.OBJECT_ID);
                String url = jsonObject.getJSONObject(RGroceryPhoto.JsonKeys.PHOTO)
                        .getString(RGroceryPhoto.JsonKeys.URL);

                Realm realmAddGroceryPhoto = Realm.getInstance(MyApplication.getContext());
                realmAddGroceryPhoto.beginTransaction();

                RGroceryPhoto rGroceryPhoto = new RGroceryPhoto();

                rGroceryPhoto.setGroceryPhotoId(groceryPhotoId);
                rGroceryPhoto.setGroceryId(groceryId);
                rGroceryPhoto.setUrl(url);

                realmAddGroceryPhoto.copyToRealmOrUpdate(rGroceryPhoto);
                realmAddGroceryPhoto.commitTransaction();
                realmAddGroceryPhoto.close();

                return null;
            }
        };

        Continuation<Void, Void> addGroceryPhotoToRealmOnSuccess = new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in addGroceryPhotoToRealmOnSuccess", exception);
                    throw exception;
                }
                return null;
            }
        };

        String photoName = Utilities.dateToString(new Date(), MyApplication.getContext()
                .getString(R.string.photo_date_format_string)) + ".jpg";

        SyncPhoto.uploadPhoto(photoName, data) // return photo name
                .continueWithTask(addGroceryPhotoItem) // return objectId(groceryPhotoId)
                .continueWithTask(getGroceryPhotoById) // return single GroceryPhoto object
                .continueWith(addGroceryPhotoToRealm) // return null
                .continueWith(addGroceryPhotoToRealmOnSuccess); // return null

        return null;
    }

    public static Task<Void> getAllGroceryPhotosByGroupId(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            Log.e(TAG, "groupId is null");
            return null;
        }

        Task<JSONObject>.TaskCompletionSource tcs = Task.create();
        UrlTemplate template = UrlTemplateCreator.getGroceryPhotoByGroupId(groupId);
        NetworkRequest networkRequest = new NetworkRequest(template, tcs);

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
                            JSONObject groceryPhotoJsonObj = groceryPhotoJsonArray.getJSONObject(i);
                            RGroceryPhoto rGroceryPhoto = new RGroceryPhoto();

                            rGroceryPhoto.setGroceryPhotoId(groceryPhotoJsonObj
                                    .getString(RGroceryPhoto.JsonKeys.OBJECT_ID));
                            rGroceryPhoto.setGroceryId(groceryPhotoJsonObj.getJSONObject(
                                    RGroceryPhoto.JsonKeys.GROCERY_ID).getString(JsonKeys.OBJECT_ID));
                            rGroceryPhoto.setUrl(groceryPhotoJsonObj.getJSONObject(
                                    RGroceryPhoto.JsonKeys.PHOTO).getString(RGroceryPhoto.JsonKeys.URL));

                            realm.copyToRealmOrUpdate(rGroceryPhoto);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing grocery photo object", e);
                        }
                    }
                    Log.d(TAG, "begin commitTransaction");
                    realm.commitTransaction();
                } catch (JSONException e) {
                    realm.cancelTransaction();
                    throw new InvalidResponseException("Error getting grocery photo object from server");
                }
                realm.close();

                return null;
            }
        };

        Continuation<Void, Void> addGroceryPhotoToRealmOnSuccess = new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in addGroceryPhotoToRealmOnSuccess", exception);
                    throw exception;
                }
                return null;
            }
        };

        networkRequest.runNetworkRequest()
                .continueWith(addGroceryPhotoToRealm)
                .continueWith(addGroceryPhotoToRealmOnSuccess);

        return null;
    }
}
