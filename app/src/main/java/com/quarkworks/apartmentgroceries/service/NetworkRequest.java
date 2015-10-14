package com.quarkworks.apartmentgroceries.service;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by zz on 10/14/15.
 */
public class NetworkRequest extends AsyncTask<Void, String, String> {

    private static final String LOG_TAG = NetworkRequest.class.getSimpleName();

    // set up the input url
    private String url;
    private Request request;
    private Callback callback;

    public NetworkRequest(String url, Callback callback) {
        this.url = url;
        this.callback = callback;
    }

    public NetworkRequest(Request request, Callback callback) {
        this.request = request;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(Void... params) {

        OkHttpClient httpClient = new OkHttpClient();
        if (request == null) {
            request = new Request.Builder().url(url).build();

        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Parse-Application-Id", "GlvuJjSGKTkc3DxedowpvgCMNOZeGQjxvRApSqGD")
                .addHeader("X-Parse-REST-API-Key", "0b3HDSEgt3EgXxyDHLOV0M7yQZwsexVG8ryTqzKI")
                .addHeader("X-Parse-Revocable-Session", "1")
                .method("GET", null) // GET method not allow request body so we pass username and secret in url directly
                .build();
        try {
            Response response = httpClient.newCall(request).execute();
            return  response.body().string();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to url: " + url, e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String response) {

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(response);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parssing JSON from url: " + url, e);
        }

        callback.done(jsonObject);
    }

    public interface Callback {
        void done(@Nullable JSONObject jsonObject);
    }
}
