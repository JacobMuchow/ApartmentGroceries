package com.quarkworks.apartmentgroceries.service;

import android.support.annotation.Nullable;
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
        NetworkRequestBolts networkRequestBolts = new NetworkRequestBolts(template, taskCompletionSource);

        return networkRequestBolts.runNetworkRequestBolts().continueWith(new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                JSONObject jsonObject = task.getResult();
                if (jsonObject == null) {
                    Log.e(TAG, "Error getting group from server");
                    return null;
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
                } catch(JSONException e) {
                    Log.e(TAG, "Error parsing group object", e);
                    realm.cancelTransaction();
                }

                return null;
            }
        });
    }

    public static Task<Boolean> add(RGroup rRGroup) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.addGroup(rRGroup);
        NetworkRequestBolts networkRequestBolts = new NetworkRequestBolts(template, taskCompletionSource);


        return networkRequestBolts.runNetworkRequestBolts().continueWith(new Continuation<JSONObject, Boolean>() {
            @Override
            public Boolean then(Task<JSONObject> task) throws Exception {
                JSONObject jsonObject = task.getResult();

                if (jsonObject == null) {
                    Log.e(TAG, "Error adding group object");
                    return false;
                }

                try {
                    String groupId = jsonObject.getString(JsonKeys.OBJECT_ID);
                    if (!TextUtils.isEmpty(groupId)) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "adding group failed", e);
                    return false;
                }
            }
        });
    }
}
