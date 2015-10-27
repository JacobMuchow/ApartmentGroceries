package com.quarkworks.apartmentgroceries.service;

import android.text.TextUtils;
import android.util.Log;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.service.models.RGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.realm.Realm;

/**
 * Created by zz on 10/16/15.
 */
public class SyncGroup {
    private static final String TAG = SyncGroup.class.getSimpleName();

    private static final class JsonKeys {
        private static final String NAME = "name";
        private static final String OBJECT_ID = "objectId";
        private static final String RESULTS = "results";
    }

    public static Task<Void> getAll() {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.getAllGroup();
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation addGroupsToRealm = new Continuation<JSONObject, Void>() {
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
                realm.clear(RGroup.class);

                try {
                    JSONArray groupJsonArray = jsonObject.getJSONArray(JsonKeys.RESULTS);

                    for (int i = 0; i < groupJsonArray.length(); i++) {
                        try {
                            RGroup groupItem = realm.createObject(RGroup.class);
                            JSONObject groupJsonObj = groupJsonArray.getJSONObject(i);
                            groupItem.setGroupId(groupJsonObj.getString(JsonKeys.OBJECT_ID));
                            groupItem.setName(groupJsonObj.getString(JsonKeys.NAME));
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing group object", e);
                        }
                    }

                    realm.commitTransaction();
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing group object", e);
                    realm.cancelTransaction();
                }
                realm.close();

                return null;
            }
        };

        return networkRequest.runNetworkRequest().continueWith(addGroupsToRealm);
    }

    public static Task<Void> add(RGroup rRGroup) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.addGroup(rRGroup);
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Void> checkAddGroup = new Continuation<JSONObject, Void>() {
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
                    if(TextUtils.isEmpty(jsonObject.getString(JsonKeys.OBJECT_ID))) {
                        throw new InvalidResponseException("Incorrect response");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing group object", e);
                }

                return null;
            }
        };

        return networkRequest.runNetworkRequest().continueWith(checkAddGroup);
    }
}
