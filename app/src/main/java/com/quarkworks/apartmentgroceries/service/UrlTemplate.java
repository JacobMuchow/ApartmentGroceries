package com.quarkworks.apartmentgroceries.service;

import java.util.Map;

/**
 * @author jacobamuchow@gmail.com (Jacob Muchow)
 */
public class UrlTemplate {
    private static final String TAG = UrlTemplate.class.getSimpleName();

    private String method;
    private String url;
    private Map<String, String> params;
    private Map<String, byte[]> paramsByte;
    private boolean useToken;

    public UrlTemplate(String method, String url, Map<String, String> params) {
        this(method, url, params, true);
    }

    public UrlTemplate(String method, String url, Map<String, String> params, boolean useToken) {
        this.method = method;
        this.url = url;
        this.params = params;
        this.useToken = useToken;
    }

    public UrlTemplate(String method, String url, Map<String, String> params, Map<String, byte[]> paramsByte, boolean useToken) {
        this.method = method;
        this.url = url;
        this.params = params;
        this.paramsByte = paramsByte;
        this.useToken = useToken;
    }


    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Map<String, byte[]> getParamsByte() {
        return paramsByte;
    }

    public void setParamsByte(Map<String, byte[]> paramsByte) {
        this.paramsByte = paramsByte;
    }

    public boolean useToken() {
        return useToken;
    }
}
