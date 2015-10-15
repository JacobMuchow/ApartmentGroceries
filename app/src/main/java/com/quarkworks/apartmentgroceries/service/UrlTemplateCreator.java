package com.quarkworks.apartmentgroceries.service;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jacobamuchow@gmail.com (Jacob Muchow)
 */
public class UrlTemplateCreator {
    private static final String TAG = UrlTemplateCreator.class.getSimpleName();

    private static final String baseUrl = "https://api.parse.com/1/";

    private static final String GET = "GET";
    private static final String POST = "POST";

    public static UrlTemplate login(String username, String password) {
        String url = baseUrl + "login";
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        return new UrlTemplate(GET, url, params);
    }

    public static UrlTemplate logout() {
        String url = baseUrl + "logout";

        return new UrlTemplate(GET, url, null);
    }

    public static UrlTemplate getAllGroceryItem() {
        String url = baseUrl + "classes/GroceryItem";

        return new UrlTemplate(GET, url, null);
    }
}
