package com.quarkworks.apartmentgroceries.service.models;

import io.realm.RealmObject;

/**
 * Created by zz on 10/14/15.
 */
public class RGroceryItem extends RealmObject {
    private String name;
    private String groupId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
