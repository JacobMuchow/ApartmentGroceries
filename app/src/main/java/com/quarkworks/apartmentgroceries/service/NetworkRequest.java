package com.quarkworks.apartmentgroceries.service;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
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
    private UrlTemplate template;
    private Callback callback;

    public NetworkRequest(UrlTemplate template, Callback callback) {
        this.template = template;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(Void... params) {

        Request.Builder builder = new Request.Builder()
                .url(template.getUrl())
                .addHeader("X-Parse-Application-Id", "GlvuJjSGKTkc3DxedowpvgCMNOZeGQjxvRApSqGD")
                .addHeader("X-Parse-REST-API-Key", "0b3HDSEgt3EgXxyDHLOV0M7yQZwsexVG8ryTqzKI")
                .addHeader("X-Parse-Revocable-Session", "1")
                .method(template.getMethod(), null);

        //TODO: add parameters from template

        if(template.useToken()) {
            //TODO: add token to builder
        }

        try {
            Response response = new OkHttpClient().newCall(builder.build()).execute();
            return  response.body().string();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to url: " + template.getUrl(), e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String response) {

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(response);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parssing JSON from url: " + template.getUrl(), e);
        }

        callback.done(jsonObject);
    }

    public interface Callback {
        void done(@Nullable JSONObject jsonObject);
    }
}
