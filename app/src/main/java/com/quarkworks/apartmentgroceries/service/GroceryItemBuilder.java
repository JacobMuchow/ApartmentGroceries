package com.quarkworks.apartmentgroceries.service;

import java.util.ArrayList;

/**
 * Created by zhao on 10/30/15.
 */
public class GroceryItemBuilder {
    private String groceryName;
    private String groupId;
    private String createdBy;
    private ArrayList<byte[]> photoList;

    public GroceryItemBuilder() {}

    public String getGroceryName() {
        return groceryName;
    }

    public void setGroceryName(String groceryName) {
        this.groceryName = groceryName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ArrayList<byte[]> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(ArrayList<byte[]> photoList) {
        this.photoList = photoList;
    }
}
