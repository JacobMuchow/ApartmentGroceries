package com.quarkworks.apartmentgroceries.service.models;

import io.realm.RealmObject;

/**
 * Created by zz on 10/14/15.
 */
public class RGroup extends RealmObject {

    public static final class JsonKeys {
        public static final String GROUP_ID = "groupId";
    }

    private String groupId;
    private String name;

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