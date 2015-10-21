package com.quarkworks.apartmentgroceries.service;

import android.util.Log;

import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.RGroup;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jacobamuchow@gmail.com (Jacob Muchow)
 */
public class UrlTemplateCreator {
    private static final String TAG = UrlTemplateCreator.class.getSimpleName();

    private static final String baseUrl = "https://api.parse.com/1/";

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String INCLUDE = "include";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String OBJECT_ID = "objectId";


    public static UrlTemplate login(String username, String password) {
        String url = baseUrl + "login";
        Map<String, String> params = new HashMap<>();
        params.put(USERNAME, username);
        params.put(PASSWORD, password);

        return new UrlTemplate(GET, url, params, false);
    }

    public static UrlTemplate signUp(String username, String password) {
        String url = baseUrl + "users";
        Map<String, String> params = new HashMap<>();
        params.put(USERNAME, username);
        params.put(PASSWORD, password);

        return new UrlTemplate(POST, url, params, false);
    }

    public static UrlTemplate logout() {
        String url = baseUrl + "logout";

        return new UrlTemplate(POST, url, null);
    }

    public static UrlTemplate getAllGroceryItems() {
        String url = baseUrl + "classes/GroceryItem";
        Map<String, String> params = new HashMap<>();
        params.put(INCLUDE, "createdBy,groupId,purchasedBy");

        return new UrlTemplate(GET, url, params);
    }

    public static UrlTemplate getGroceryItemsByGroupId(String groupId) {
        String url = baseUrl + "classes/GroceryItem";
        Map<String, String> params = new HashMap<>();

        JSONObject subGroupIdObj = new JSONObject();
        JSONObject groupIdObj=new JSONObject();
        try {
            subGroupIdObj.put("__type", "Pointer");
            subGroupIdObj.put("className", "Group");
            subGroupIdObj.put("objectId", groupId);
            groupIdObj.put("groupId", subGroupIdObj);
        } catch (JSONException e) {
            Log.d(TAG, "Error creating group id object for where in getGroceryItemsByGroupId", e);
        }

        params.put("where", Utilities.encodeURIComponent(groupIdObj.toString()));
        return new UrlTemplate(GET, url, params);
    }

    public static UrlTemplate getAllUsers() {
        String url = baseUrl + "users";

        return new UrlTemplate(GET, url, null);
    }

    public static UrlTemplate getAllGroup() {
        String url = baseUrl + "classes/Group";
        return new UrlTemplate(GET, url, null);
    }

    public static UrlTemplate addGroceryItem(RGroceryItem rGroceryItem) {
        String url = baseUrl + "classes/GroceryItem";
        Map<String, String> params = new HashMap<>();

        params.put("name", rGroceryItem.getName());

        JSONObject groupIdObj=new JSONObject();
        try {
            groupIdObj.put("__type", "Pointer");
            groupIdObj.put("className", "Group");
            groupIdObj.put("objectId", rGroceryItem.getGroupId());
            params.put("groupId", groupIdObj.toString());
        } catch (JSONException e) {
            Log.d(TAG, "Error creating group id object for where in addGroceryItem", e);
        }

        JSONObject createdByObj = new JSONObject();
        try {
            createdByObj.put("__type", "Pointer");
            createdByObj.put("className", "_User");
            createdByObj.put("objectId", rGroceryItem.getCreatedBy());
            params.put("createdBy", createdByObj.toString());
        } catch (JSONException e) {
            Log.d(TAG, "Error creating created by object for where in addGroceryItem", e);
        }

        return new UrlTemplate(POST, url, params);
    }

    public static UrlTemplate addGroup(RGroup rGroup) {
        String url = baseUrl + "classes/Group";
        Map<String, String> params = new HashMap<>();

        params.put("name", rGroup.getName());

        return new UrlTemplate(POST, url, params);
    }

    public static UrlTemplate joinGroup(String userId, String groupId) {
        String url = baseUrl + "users";
        Map<String, String> params = new HashMap<>();
        JSONObject groupObject = new JSONObject();

        try {
            groupObject.put("__type", "Pointer");
            groupObject.put("className", "Group");
            groupObject.put("objectId", groupId);

            Log.d(TAG, "groupObject:" + groupObject.toString());
            params.put("groupId", groupObject.toString());
            params.put("objectId", userId);

            return new UrlTemplate(PUT, url, params, true);

        } catch (JSONException e) {
            Log.d(TAG, "Error creating group object for where in joinGroup()", e);
        }

        return null;
    }

    public static UrlTemplate updateProfilePhoto(String userId, String photoName) {
        String url = baseUrl + "users";
        Map<String, String> params = new HashMap<>();
        JSONObject photoObject = new JSONObject();

        try {
            photoObject.put("__type", "File");
            photoObject.put("name", photoName);

            Log.d(TAG, "photoObject:" + photoObject.toString());
            params.put("photo", photoObject.toString());
            params.put("objectId", userId);

            return new UrlTemplate(PUT, url, params, true);

        } catch (JSONException e) {
            Log.d(TAG, "Error creating photo object for where clause in updateProfilePhoto()", e);
        }

        return null;
    }

    public static UrlTemplate uploadProfilePhoto(String photoName, byte[] content) {
        String url = baseUrl + "files/" + photoName;
        Map<String, byte[]> params = new HashMap<>();
        params.put("content", content);

        return new UrlTemplate(POST, url, null, params, false);
    }
}
