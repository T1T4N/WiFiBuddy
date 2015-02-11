package com.titantech.wifibuddy.network.requests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by Robert on 29.01.2015.
 */

public class PutRestRequest extends RestRequest {
    public PutRestRequest(String requestUrl, Map<String, String> putData) {
        this(requestUrl, putData, null, null, 1000);
    }

    public PutRestRequest(String requestUrl, Map<String, String> putData, String authUsername, String authPassword) {
        this(requestUrl, putData, authUsername, authPassword, 1000);
    }

    public PutRestRequest(String requestUrl, Map<String, String> putData, String authUsername, String authPassword, int timeout) {
        this.requestUrl = requestUrl;
        this.requestType = RequestType.PUT;
        this.requestTimeout = timeout;
        this.encodedAuthorization = encodeCredentials(authUsername, authPassword);
        this.requestData = encodeParameters(putData);
    }

    private String encodeParameters(Map<String, String> params) {
        StringBuilder postData = new StringBuilder();
        try {
            for (Map.Entry<String, String> iter : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');

                postData.append(URLEncoder.encode(iter.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(iter.getValue(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return postData.toString();
    }

    @Override
    protected HttpURLConnection setupConnection() throws IOException {
        byte[] putDataBytes = requestData.getBytes("UTF-8");

        URL u = new URL(requestUrl);
        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.setRequestMethod(requestType);

        c.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

        c.setUseCaches(false);
        c.setAllowUserInteraction(false);
        c.setConnectTimeout(requestTimeout);
        c.setReadTimeout(requestTimeout);

        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        c.setRequestProperty("Content-Length", String.valueOf(putDataBytes.length));
        c.getOutputStream().write(putDataBytes);
        return c;
    }
}