package com.quarkworks.apartmentgroceries.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
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
        RequestBody requestBody = null;
        Map<String, String> paramsMap = template.getParams();
        Map<String, byte[]> parmasMapByte = template.getParamsByte();

        if (paramsMap != null) {
            String objectId = paramsMap.get(UrlTemplateCreator.OBJECT_ID);
            if (objectId != null) {
                url = url + "/" + objectId;
                paramsMap.remove(UrlTemplateCreator.OBJECT_ID);
            }
        }

        // add parameters to url if using GET, or we build RequestBody
        if (method.equals(UrlTemplateCreator.GET)) {
            StringBuilder urlStringBuilder = new StringBuilder(url);
            urlStringBuilder.append("?");

            if (paramsMap != null) {
                for (Map.Entry entry : paramsMap.entrySet()) {
                    urlStringBuilder.append(entry.getKey() + "=" + entry.getValue() + "&");
                }
            }
            url = urlStringBuilder.toString();
        } else if (parmasMapByte != null) {
            requestBody = RequestBody.create(MediaType.parse("image/jpeg"), parmasMapByte.get("content"));
        } else {
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            if (paramsMap != null) {
                int size = paramsMap.size();
                int i = 0;
                for (Map.Entry entry : paramsMap.entrySet()) {
                    String entryValue = entry.getValue().toString();
                    if(entryValue.charAt(0) == '{') {
                        jsonBuilder.append("\"" + entry.getKey() + "\":" + entry.getValue() + "");
                    } else {
                        jsonBuilder.append("\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"");
                    }

                    if (i < size - 1) {
                        jsonBuilder.append(",");
                    }
                    i++;
                }
            }
            jsonBuilder.append("}");
            Log.d(TAG, "jsonBuilder:" + jsonBuilder);
            MediaType ContentTypeJSON = MediaType.parse("application/json; charset=utf-8");
            requestBody = RequestBody.create(ContentTypeJSON, jsonBuilder.toString());
        }

        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("X-Parse-Application-Id", "GlvuJjSGKTkc3DxedowpvgCMNOZeGQjxvRApSqGD")
                .addHeader("X-Parse-REST-API-Key", "0b3HDSEgt3EgXxyDHLOV0M7yQZwsexVG8ryTqzKI")
                .addHeader("X-Parse-Revocable-Session", "1")
                .method(method, requestBody);


        Log.d(TAG, builder.toString());
        if(template.useToken()) {
            Context context = MyApplication.getContext();
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(
                            context.getString(R.string.login_or_sign_up_session), 0);
            String sessionToken = sharedPreferences.getString("sessionToken", null);
            if (sessionToken == null) {
                Log.e(TAG, "Error getting session token.");
                return null;
            }
            builder.addHeader("X-Parse-Session-Token", sessionToken);
        }

        if (parmasMapByte != null) {
            builder.addHeader("Content-Type", "image/jpeg");
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

        try {
            if (!TextUtils.isEmpty(response)) {
                JSONObject jsonObject = new JSONObject(response);
                callback.done(jsonObject);
            } else {
                Log.e(TAG, "The response is null in onPostExecute");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON from url: " + template.getUrl(), e);
        }
    }

    public interface Callback {
        void done(@Nullable JSONObject jsonObject);
    }
}
