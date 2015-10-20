package com.quarkworks.apartmentgroceries.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
/**
 * Created by zz on 10/15/15.
 */
public class Utilities
{
    public static String encodeURIComponent(String s) {
        String result = null;

        try
        {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    private Utilities() {
        super();
    }
}
