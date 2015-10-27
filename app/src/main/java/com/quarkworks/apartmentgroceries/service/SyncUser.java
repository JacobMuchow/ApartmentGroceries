package com.quarkworks.apartmentgroceries.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.models.RUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.realm.Realm;

import com.quarkworks.apartmentgroceries.service.models.RUser.JsonKeys;

/**
 * Created by zz on 10/15/15.
 */
public class SyncUser {
    private static final String TAG = SyncUser.class.getSimpleName();

    public static Task<Void> login(String username, String password) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.login(username, password);
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Void> saveLoginCredential = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in login", exception);
                    throw exception;
                }

                JSONObject loginJsonObj = task.getResult();

                if (loginJsonObj == null) {
                    throw new InvalidResponseException("Empty response");
                }

                String sessionToken = loginJsonObj.optString(JsonKeys.SESSION_TOKEN);
                String userId = loginJsonObj.optString(JsonKeys.OBJECT_ID);

                if (!TextUtils.isEmpty(sessionToken)) {
                    Context context = MyApplication.getContext();
                    SharedPreferences sharedPreferences = context
                            .getSharedPreferences(context.getString(R.string.login_or_sign_up_session), 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(JsonKeys.SESSION_TOKEN, sessionToken);
                    editor.putString(JsonKeys.USER_ID, userId);

                    JSONObject groupIdObj = loginJsonObj.optJSONObject(JsonKeys.GROUP_ID);
                    if (groupIdObj != null) {
                        String groupId = groupIdObj.optString(JsonKeys.OBJECT_ID);
                        editor.putString(JsonKeys.GROUP_ID, groupId);
                    }
                    editor.apply();
                } else {
                    String error = loginJsonObj.optString(JsonKeys.ERROR);
                    if (!TextUtils.isEmpty(error)) {
                        throw new InvalidResponseException(error);
                    } else {
                        throw new InvalidResponseException("Incorrect login response");
                    }
                }

                return null;
            }
        };

        return networkRequest.runNetworkRequest().continueWith(saveLoginCredential);
    }

    public static Task<Void> logout() {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.logout();
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Void> checkLogout = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in logout", exception);
                    throw exception;
                }

                JSONObject logoutJsonObj = task.getResult();

                if (logoutJsonObj != null && logoutJsonObj.toString().equals("{}")) {
                    return null;
                } else {
                    throw new InvalidResponseException("Incorrect logout response");
                }
            }
        };

        return networkRequest.runNetworkRequest().continueWith(checkLogout);
    }

    public static Task<Void> signUp(String username, String password) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.signUp(username, password);
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Void> saveCredential = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in signUp", exception);
                    throw exception;
                }

                JSONObject signUpJsonObj = task.getResult();

                if (signUpJsonObj == null) {
                    throw new InvalidResponseException("Empty response");
                }

                try {
                    String sessionToken = signUpJsonObj.getString(JsonKeys.SESSION_TOKEN);

                    Context context = MyApplication.getContext();
                    SharedPreferences sharedPreferences = context
                            .getSharedPreferences(context.getString(R.string.login_or_sign_up_session), 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(JsonKeys.SESSION_TOKEN, sessionToken);
                    editor.apply();
                } catch (JSONException e) {
                    throw new InvalidResponseException("Incorrect sign up response");
                }

                return null;
            }
        };

        return networkRequest.runNetworkRequest().continueWith(saveCredential);
    }

    public static Task<Void> getAll(String groupId){

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template;
        if (TextUtils.isEmpty(groupId)) {
            template = UrlTemplateCreator.getAllUsers();
        } else {
            template = UrlTemplateCreator.getUsersByGroupId(groupId);
        }
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Void> addUsersToRealm = new Continuation<JSONObject, Void>() {
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
                realm.clear(RUser.class);

                try {
                    JSONArray userJsonArray = jsonObject.getJSONArray(JsonKeys.RESULTS);
                    for (int i = 0; i < userJsonArray.length(); i++) {
                        RUser rUser = realm.createObject(RUser.class);
                        try {
                            JSONObject userJsonObj = userJsonArray.getJSONObject(i);

                            rUser.setUserId(userJsonObj.getString(JsonKeys.OBJECT_ID));
                            rUser.setUsername(userJsonObj.getString(JsonKeys.USERNAME));
                            rUser.setEmail(userJsonObj.optString(JsonKeys.EMAIL));
                            rUser.setPhone(userJsonObj.optString(JsonKeys.PHONE));

                            JSONObject groupIdObj = userJsonObj.optJSONObject(JsonKeys.GROUP_ID);
                            if (groupIdObj != null) {
                                rUser.setGroupId(groupIdObj.getString(JsonKeys.OBJECT_ID));
                            }
                            rUser.setPhone(userJsonObj.optString(JsonKeys.PHONE));
                            JSONObject photoJsonObj = userJsonObj.optJSONObject(JsonKeys.PHOTO);
                            if (photoJsonObj != null) {
                                rUser.setUrl(photoJsonObj.getString(JsonKeys.URL));
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing user object", e);
                        }
                    }

                    realm.commitTransaction();
                } catch (JSONException e) {
                    Log.e(TAG, "Error get user object from server", e);
                    realm.cancelTransaction();
                }
                realm.close();

                return null;
            }
        };

        return networkRequest.runNetworkRequest().continueWith(addUsersToRealm);
    }

    public static Task<Void> getAll() {
        return getAll(null);
    }

    public static Task<Void> joinGroup(String userId, String groupId) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.joinGroup(userId, groupId);
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Void> checkJoiningGroup = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in joinGroup", exception);
                    throw exception;
                }

                JSONObject jsonObject = task.getResult();
                if (jsonObject == null) {
                    throw new InvalidResponseException("Empty response");
                }

                try {
                    if (TextUtils.isEmpty(jsonObject.getString(JsonKeys.UPDATED_AT))) {
                        throw new InvalidResponseException("Invalid join group response");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing joining group response", e);
                }

                return null;
            }
        };

        return networkRequest.runNetworkRequest().continueWith(checkJoiningGroup);
    }

    public static Task<JSONObject> updateProfilePhoto(String photoName, byte[] data) {

        SharedPreferences sharedPreferences =
                MyApplication.getContext().getSharedPreferences(
                        MyApplication.getContext()
                                .getString(R.string.login_or_sign_up_session), 0);
        final String userId = sharedPreferences.getString(JsonKeys.USER_ID, null);

        final Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();

        Continuation<JSONObject, Void> updateUserPhoto = new Continuation<JSONObject, Void>() {
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

                final UrlTemplate template = UrlTemplateCreator.updateProfilePhoto(userId, photoName);

                Continuation<JSONObject, Void> UpdatingPhoto = new Continuation<JSONObject, Void>() {
                    @Override
                    public Void then(Task<JSONObject> task) throws Exception{
                        if (task.isFaulted()) {
                            Exception exception = task.getError();
                            Log.e(TAG, "Error in UpdatingPhoto", exception);
                            throw exception;
                        }

                        try {
                            String updatedAt = task.getResult().getString(RUser.JsonKeys.UPDATED_AT);
                            if (!TextUtils.isEmpty(updatedAt)) {
                                Context context = MyApplication.getContext();
                                SharedPreferences sharedPreferences = context.getSharedPreferences(
                                        context.getString(R.string.login_or_sign_up_session), 0);
                                String userId = sharedPreferences.getString(RUser.JsonKeys.USER_ID, null);
                                SyncUser.getById(userId);
                            } else {
                                throw new InvalidResponseException("Incorrect updating user's photo response");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing updating photo response", e);
                        }
                        return null;
                    }
                };
                new NetworkRequest(template, taskCompletionSource).runNetworkRequest().continueWith(UpdatingPhoto);

                return null;
            }
        };

        SyncPhoto.uploadPhoto(photoName, data).continueWith(updateUserPhoto);

        return taskCompletionSource.getTask();
    }

    public static Task getById(String userId) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.getSingleUser(userId);
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, RUser> addSingleUserToRealm = new Continuation<JSONObject, RUser>() {
            @Override
            public RUser then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    Exception exception = task.getError();
                    Log.e(TAG, "Error in getById", exception);
                    throw exception;
                }

                if (task.getResult() == null) {
                    throw new InvalidResponseException("Empty response");
                }

                try {
                    JSONObject userJsonObj = new JSONObject(task.getResult().toString());

                    RUser rUser = new RUser();
                    rUser.setUserId(userJsonObj.getString(JsonKeys.OBJECT_ID));
                    rUser.setUsername(userJsonObj.getString(JsonKeys.USERNAME));
                    rUser.setEmail(userJsonObj.optString(JsonKeys.EMAIL));
                    rUser.setPhone(userJsonObj.optString(JsonKeys.PHONE));

                    JSONObject groupIdObj = userJsonObj.optJSONObject(JsonKeys.GROUP_ID);
                    if (groupIdObj != null) {
                        rUser.setGroupId(groupIdObj.getString(JsonKeys.OBJECT_ID));
                    }
                    rUser.setPhone(userJsonObj.optString(JsonKeys.PHONE));
                    JSONObject photoJsonObj = userJsonObj.optJSONObject(JsonKeys.PHOTO);
                    if (photoJsonObj != null) {
                        rUser.setUrl(photoJsonObj.getString(JsonKeys.URL));
                    }

                    Realm realmInsert = Realm.getInstance(MyApplication.getContext());
                    realmInsert.beginTransaction();
                    realmInsert.copyToRealmOrUpdate(rUser);
                    realmInsert.commitTransaction();
                    realmInsert.close();

                    return rUser;

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing user object", e);
                }

                return null;
            }
        };

        return networkRequest.runNetworkRequest().continueWith(addSingleUserToRealm);
    }
}
