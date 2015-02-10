package com.titantech.wifibuddy.network.requests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Robert on 23.01.2015.
 */
public class PostRestRequest extends RestRequest {
    public PostRestRequest(String requestUrl, Map<String, String> postData) {
        this(requestUrl, postData, null, null, 1000);
    }

    public PostRestRequest(String requestUrl, Map<String, String> postData, String authUsername, String authPassword) {
        this(requestUrl, postData, authUsername, authPassword, 1000);
    }

    public PostRestRequest(String requestUrl, Map<String, String> postData, String authUsername, String authPassword, int timeout) {
        this.requestUrl = requestUrl;
        this.requestType = RequestType.POST;
        this.requestTimeout = timeout;
        this.encodedAuthorization = encodeCredentials(authUsername, authPassword);
        this.requestData = encodeParameters(postData);
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
        byte[] postDataBytes = requestData.getBytes("UTF-8");

        URL u = new URL(requestUrl);
        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.setRequestMethod(requestType);

        if (encodedAuthorization.length() > 0) // Optional, when creating user
            c.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

        c.setUseCaches(false);
        c.setAllowUserInteraction(false);
        c.setConnectTimeout(requestTimeout);
        c.setReadTimeout(requestTimeout);

        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        c.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        c.getOutputStream().write(postDataBytes);
        return c;
    }
}
