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

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

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

        return new UrlTemplate(GET, url, null);
    }

    public static UrlTemplate getAllGroceryItems() {
        String url = baseUrl + "classes/GroceryItem";

        return new UrlTemplate(GET, url, null);
    }

    public static UrlTemplate getAllUsers() {
        String url = baseUrl + "users";

        return new UrlTemplate(GET, url, null);
    }
}
