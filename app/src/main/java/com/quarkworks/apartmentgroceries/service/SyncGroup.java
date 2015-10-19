package com.quarkworks.apartmentgroceries.service;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.quarkworks.apartmentgroceries.service.models.RGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public static Promise getAll() {

        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {
                if (jsonObject == null) {
                    Log.e(TAG, "Error getting group from server");
                    promise.onFailure();
                    return;
                }

                Realm realm = DataStore.getInstance().getRealm();
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
                    promise.onSuccess();
                } catch(JSONException e) {
                    Log.e(TAG, "Error parsing group object", e);
                    realm.cancelTransaction();
                    promise.onFailure();
                }
            }
        };

        UrlTemplate template = UrlTemplateCreator.getAllGroup();
        new NetworkRequest(template, callback).execute();
        return promise;
    }

    public static Promise add(RGroup rRGroup) {

        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {
                if (jsonObject == null) {
                    Log.e(TAG, "Error adding group object");
                    promise.onFailure();
                    return;
                }

                try {
                    String groupId = jsonObject.getString(JsonKeys.OBJECT_ID);
                    if (!TextUtils.isEmpty(groupId)) {
                        promise.onSuccess();
                    } else {
                        promise.onFailure();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "adding group failed", e);
                    promise.onFailure();
                }
            }
        };

        UrlTemplate template = UrlTemplateCreator.addGroup(rRGroup);
        new NetworkRequest(template, callback).execute();
        return promise;
    }
}