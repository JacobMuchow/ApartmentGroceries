package com.quarkworks.apartmentgroceries.service.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by zz on 10/14/15.
 */
public class RGroceryItem extends RealmObject {

    public static final class JsonKeys {
        public static final String GROUP_ID = "groupId";
        public static final String NAME = "name";
        public static final String OBJECT_ID = "objectId";
        public static final String RESULTS = "results";
        public static final String CREATED_BY = "createdBy";
        public static final String PURCHASED_BY = "purchasedBy";
        public static final String CREATED_AT ="createdAt";
    }

    @PrimaryKey
    private String groceryId;
    private String name;
    private String groupId;
    private String groupName;
    private String createdBy;
    private String purchasedBy;
    private String createdAt;

    public String getGroceryId() {
        return groceryId;
    }

    public void setGroceryId(String groceryId) {
        this.groceryId = groceryId;
    }

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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getPurchasedBy() {
        return purchasedBy;
    }

    public void setPurchasedBy(String purchasedBy) {
        this.purchasedBy = purchasedBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
