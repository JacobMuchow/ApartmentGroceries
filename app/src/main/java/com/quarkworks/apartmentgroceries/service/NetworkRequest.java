package com.quarkworks.apartmentgroceries.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * Created by zz on 10/14/15.
 */
public class NetworkRequest extends AsyncTask<Void, String, String> {

    private static final String TAG = NetworkRequest.class.getSimpleName();

    private UrlTemplate template;
    private Callback callback;

    public NetworkRequest(UrlTemplate template, Callback callback) {
        this.template = template;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(Void... params) {

        String url = template.getUrl();
        String method = template.getMethod();

        // add parameters from template
        StringBuilder urlStringBuilder = new StringBuilder(url);
        urlStringBuilder.append("?");

        Map<String, String> paramsMap = template.getParams();
        if (paramsMap != null) {
            for (Map.Entry entry : paramsMap.entrySet()) {
                urlStringBuilder.append(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }

        Request.Builder builder = new Request.Builder()
                .url(urlStringBuilder.toString())
                .addHeader("X-Parse-Application-Id", "GlvuJjSGKTkc3DxedowpvgCMNOZeGQjxvRApSqGD")
                .addHeader("X-Parse-REST-API-Key", "0b3HDSEgt3EgXxyDHLOV0M7yQZwsexVG8ryTqzKI")
                .addHeader("X-Parse-Revocable-Session", "1")
                .method(method, null);

        if(template.useToken() && !method.equals("GET")) {
            Context context = MyApplication.getContext();
            SharedPreferences sharedPreferences = context.getSharedPreferences("login", 0);
            String sessionToken = sharedPreferences.getString("sessionToken", null);
            builder.addHeader("X-Parse-Session-Token", sessionToken);
        }

        try {
            Response response = new OkHttpClient().newCall(builder.build()).execute();
            return  response.body().string();
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to url: " + template.getUrl(), e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String response) {

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(response);
        } catch (JSONException e) {
            Log.e(TAG, "Error parssing JSON from url: " + template.getUrl(), e);
        }

        callback.done(jsonObject);
    }

    public interface Callback {
        void done(@Nullable JSONObject jsonObject);
    }
}
