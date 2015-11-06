package com.quarkworks.apartmentgroceries.service;

import android.util.Log;

import com.quarkworks.apartmentgroceries.service.models.RGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
    public static final String DELETE = "DELETE";
    public static final String INCLUDE = "include";
    public static final String BATCH = "BATCH";
    public static final String CONTENT = "CONTENT";
    public static final String PUSH = "PUSH";
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

    public static UrlTemplate getAllGroceryItemsByGroupId(String groupId) {
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

        params.put(INCLUDE, "createdBy,groupId,purchasedBy");
        params.put("where", Utilities.encodeURIComponent(groupIdObj.toString()));
        return new UrlTemplate(GET, url, params);
    }

    public static UrlTemplate getAllUsers() {
        String url = baseUrl + "users";

        return new UrlTemplate(GET, url, null);
    }

    public static UrlTemplate getUsersByGroupId(String groupId) {
        String url = baseUrl + "users";
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

    public static UrlTemplate getAllGroup() {
        String url = baseUrl + "classes/Group";
        return new UrlTemplate(GET, url, null);
    }

    public static UrlTemplate addGroceryItem(GroceryItemBuilder builder) {
        String url = baseUrl + "classes/GroceryItem";
        Map<String, String> params = new HashMap<>();

        params.put("name", builder.getGroceryName());

        JSONObject groupIdObj=new JSONObject();
        try {
            groupIdObj.put("__type", "Pointer");
            groupIdObj.put("className", "Group");
            groupIdObj.put("objectId", builder.getGroupId());
            params.put("groupId", groupIdObj.toString());
        } catch (JSONException e) {
            Log.d(TAG, "Error creating group id object for where in addGroceryItem", e);
        }

        JSONObject createdByObj = new JSONObject();
        try {
            createdByObj.put("__type", "Pointer");
            createdByObj.put("className", "_User");
            createdByObj.put("objectId", builder.getCreatedBy());
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
        params.put(CONTENT, content);

        return new UrlTemplate(POST, url, null, params, false);
    }

    public static UrlTemplate getSingleUser(String userId) {
        String url = baseUrl + "users" + "/" + userId;

        return new UrlTemplate(GET, url, null);
    }

    public static UrlTemplate addGroceryPhoto(String groceryId, String photoName, String groupId) {
        String url = baseUrl + "classes/GroceryPhoto";
        Map<String, String> params = new HashMap<>();
        JSONObject groceryObject = new JSONObject();
        JSONObject photoObject = new JSONObject();
        JSONObject groupIdObj=new JSONObject();

        try {

            // grocery pointer
            groceryObject.put("__type", "Pointer");
            groceryObject.put("className", "GroceryItem");
            groceryObject.put("objectId", groceryId);

            Log.d(TAG, "groceryObject:" + groceryObject.toString());
            params.put("groceryId", groceryObject.toString());

            // photo pointer
            photoObject.put("__type", "File");
            photoObject.put("name", photoName);

            Log.d(TAG, "photoObject:" + photoObject.toString());
            params.put("photo", photoObject.toString());

            groupIdObj.put("__type", "Pointer");
            groupIdObj.put("className", "Group");
            groupIdObj.put("objectId", groupId);
            params.put("groupId", groupIdObj.toString());

            return new UrlTemplate(POST, url, params, true);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating grocery pointer or photo pointer for where clause in addGroceryPhoto()", e);
        }

        return null;
    }

    public static UrlTemplate getGroceryPhotoByGroupId(String groupId) {
        String url = baseUrl + "classes/GroceryPhoto";
        Map<String, String> params = new HashMap<>();

        JSONObject subGroupIdObj = new JSONObject();
        JSONObject groupIdObj=new JSONObject();
        try {
            subGroupIdObj.put("__type", "Pointer");
            subGroupIdObj.put("className", "Group");
            subGroupIdObj.put("objectId", groupId);
            groupIdObj.put("groupId", subGroupIdObj);
        } catch (JSONException e) {
            Log.d(TAG, "Error creating group id object for where in getGroceryPhotoByGroupId", e);
        }

        params.put("where", Utilities.encodeURIComponent(groupIdObj.toString()));
        return new UrlTemplate(GET, url, params);
    }

    public static UrlTemplate getGroceryPhotoByGroceryId(String groceryId) {
        String url = baseUrl + "classes/GroceryPhoto";
        Map<String, String> params = new HashMap<>();

        JSONObject subGroceryIdObj = new JSONObject();
        JSONObject groceryIdObj=new JSONObject();
        try {
            subGroceryIdObj.put("__type", "Pointer");
            subGroceryIdObj.put("className", "GroceryItem");
            subGroceryIdObj.put("objectId", groceryId);
            groceryIdObj.put("groceryId", subGroceryIdObj);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating groceryId id object for where in getGroceryPhotoByGroceryId", e);
        }

        params.put("where", Utilities.encodeURIComponent(groceryIdObj.toString()));
        return new UrlTemplate(GET, url, params);
    }

    public static UrlTemplate getGroceryPhotoByGroceryPhotoId(String groceryPhotoId) {
        String url = baseUrl + "classes/GroceryPhoto/" + groceryPhotoId;
        return new UrlTemplate(GET, url, null);
    }

    public static UrlTemplate deleteGroceryByGroceryId(String groceryId) {
        String url = baseUrl + "classes/GroceryItem/" + groceryId;
        return new UrlTemplate(DELETE, url, null);
    }

    public static UrlTemplate deleteGroceryPhotoByGroceryIds(ArrayList<String> groceryPhotoIds) {
        String url = baseUrl + "batch";
        Map<String, String> params = new HashMap<>();

        JSONArray requestBody = new JSONArray();
        for (int i = 0; i < groceryPhotoIds.size(); i++) {
            JSONObject bodyObj = new JSONObject();
            try {
                String path = "/1/classes/GroceryPhoto/" + groceryPhotoIds.get(i);
                bodyObj.put("method", DELETE);
                bodyObj.put("path", path);
                requestBody.put(bodyObj);
            } catch (JSONException e) {
                Log.e(TAG, "Error creating batch request body object in deleteGroceryPhotoByGroceryIds", e);
            }
        }

        JSONObject requestObj = new JSONObject();
        try {
            requestObj.put("requests", requestBody);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating batch request object in deleteGroceryPhotoByGroceryIds", e);
        }

        params.put(BATCH, requestObj.toString());

        return new UrlTemplate(POST, url, params);
    }

    public static UrlTemplate createInstallation(String deviceToken, String groupId) {
        String url = baseUrl +  "installations" ;

        Map<String, String> params = new HashMap<>();
        params.put("deviceType", "android");
        params.put("pushType", "gcm");
        params.put("deviceToken", deviceToken);
        params.put("groupId", groupId);

        return new UrlTemplate(POST, url, params);
    }

    public static UrlTemplate pushNotification(String groupId) {
        String url = baseUrl +  "push" ;

        Map<String, String> params = new HashMap<>();
        String message = "new grocery";
        JSONObject messageObj = new JSONObject();

        try {
            messageObj.put("alert", message);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating message object in pushNotification", e);
        }

        JSONObject groupIdObj = new JSONObject();
        try {
            groupIdObj.put("groupId", groupId);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating group id object for where in pushNotification", e);
        }

        String pushStr = "{" + "\"where\":" + groupIdObj.toString() +  ",\"data\":" + messageObj.toString() + "}";
        params.put(PUSH, pushStr);
        return new UrlTemplate(POST, url, params);
    }

    public static UrlTemplate updateProfile(String userId, String fieldName, String fieldValue) {
        String url = baseUrl + "users";
        Map<String, String> params = new HashMap<>();

        params.put(fieldName, fieldValue);
        params.put("objectId", userId);

        return new UrlTemplate(PUT, url, params, true);
    }
}
