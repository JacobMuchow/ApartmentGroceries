package com.quarkworks.apartmentgroceries.service;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import bolts.Task;

/**
 * Created by zz on 10/21/15.
 */
public class NetworkRequestBolts {
    private static final String TAG = NetworkRequestBolts.class.getSimpleName();

    private static final Executor NETWORK_EXECUTOR = Executors.newCachedThreadPool();

    private UrlTemplate template;
    private Task<JSONObject>.TaskCompletionSource taskCompletionSource;

    public NetworkRequestBolts(UrlTemplate template, Task<JSONObject>.TaskCompletionSource taskCompletionSource) {
        this.template = template;
        this.taskCompletionSource = taskCompletionSource;
    }

    public Task runNetworkRequestBolts() {
        Task.call(new Callable<Void>() {
            public Void call() {
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
                    String jsonString = response.body().string();
                    Log.d(TAG, "jsonString:" + jsonString);
                    try {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        taskCompletionSource.setResult(jsonObject);
                    } catch (JSONException e) {

                    }

                } catch (IOException e) {
                    Log.e(TAG, "Error connecting to url: " + template.getUrl(), e);
                }
                return null;
            }
        }, NETWORK_EXECUTOR);

        return taskCompletionSource.getTask();
    }
}
