package com.quarkworks.apartmentgroceries.service.models;

import io.realm.RealmObject;

/**
 * Created by zz on 10/14/15.
 */
public class RGroceryItem extends RealmObject {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
