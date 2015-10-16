package com.quarkworks.apartmentgroceries.service.models;

import io.realm.RealmObject;

/**
 * Created by zz on 10/15/15.
 */
public class RUser extends RealmObject {
    private String userId;
    private String name;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
