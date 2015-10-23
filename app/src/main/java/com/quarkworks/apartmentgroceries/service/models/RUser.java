package com.quarkworks.apartmentgroceries.service.models;

import io.realm.RealmObject;

/**
 * Created by zz on 10/15/15.
 */
public class RUser extends RealmObject {

    public static final class JsonKeys {
        public static final String GROUP_ID = "groupId";
        public static final String OBJECT_ID = "objectId";
        public static final String RESULTS = "results";
        public static final String SESSION_TOKEN = "sessionToken";
        public static final String USERNAME = "username";
        public static final String USER_ID = "userId";
        public static final String UPDATED_AT = "updatedAt";
        public static final String EMAIL = "email";
        public static final String PHONE = "phone";
        public static final String PHOTO = "photo";
        public static final String URL = "url";
    }

    public static final class RealmKeys {
        public static final String USER_ID = "userId";
    }

    private String userId;
    private String groupId;
    private String username;
    private String email;
    private String phone;
    private String url;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
