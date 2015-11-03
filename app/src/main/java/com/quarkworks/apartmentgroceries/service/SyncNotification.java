package com.quarkworks.apartmentgroceries.service;

import android.content.SharedPreferences;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;
import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.models.RUser;

import org.json.JSONObject;

import bolts.Task;

/**
 * Created by zhao on 11/2/15.
 */
public class SyncNotification {
    private static final String TAG = SyncNotification.class.getSimpleName();


    public static Task<JSONObject> createInstallation(String deviceToken, String groupId) {
        Task<JSONObject>.TaskCompletionSource tcs = Task.create();
        UrlTemplate template = UrlTemplateCreator.createInstallation(deviceToken, groupId);
        NetworkRequest networkRequest = new NetworkRequest(template, tcs);

        return networkRequest.runNetworkRequest();
    }

    public static void updateInstallation() {
        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                String deviceToken = (String) ParseInstallation.getCurrentInstallation().get("deviceToken");
                SharedPreferences sharedPreferences = MyApplication.getContext()
                        .getSharedPreferences(MyApplication.getContext()
                                .getString(R.string.login_or_sign_up_session), 0);
                String groupId = sharedPreferences.getString(RUser.JsonKeys.GROUP_ID, null);

                SyncNotification.createInstallation(deviceToken, groupId);
            }
        });
    }
}
