package com.quarkworks.apartmentgroceries.service.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by zz on 10/14/15.
 */
public class RGroceryPhoto extends RealmObject {

    public static final class JsonKeys {
        public static final String GROCERY_ID = "groceryId";
        public static final String OBJECT_ID = "objectId";
        public static final String PHOTO = "photo";
        public static final String URL = "url";
    }

    public static final class RealmKeys {
        public static final String GROCERY_ID = "groceryId";
    }

    @PrimaryKey
    private String groceryPhotoId;
    private String groceryId;
    private String url;

    public String getGroceryPhotoId() {
        return groceryPhotoId;
    }

    public void setGroceryPhotoId(String groceryPhotoId) {
        this.groceryPhotoId = groceryPhotoId;
    }

    public String getGroceryId() {
        return groceryId;
    }

    public void setGroceryId(String groceryId) {
        this.groceryId = groceryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
