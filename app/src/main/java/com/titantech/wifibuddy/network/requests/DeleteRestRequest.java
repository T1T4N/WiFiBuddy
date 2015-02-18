package com.titantech.wifibuddy.network.requests;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DeleteRestRequest extends RestRequest {

    public DeleteRestRequest(String requestUrl, String authUsername, String authPassword) {
        this(requestUrl, authUsername, authPassword, 1000);
    }

    public DeleteRestRequest (String requestUrl, String authUsername, String authPassword, int timeout) {
        this.requestUrl = requestUrl;
        this.requestType = RequestType.DELETE;
        this.requestTimeout = timeout;
        this.encodedAuthorization = encodeCredentials(authUsername, authPassword);
        this.requestData = "";
    }

    @Override
    protected HttpURLConnection setupConnection() throws IOException {
        URL u = new URL(requestUrl);

        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.setRequestMethod(requestType);
        c.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

        c.setUseCaches(false);
        c.setAllowUserInteraction(false);
        c.setConnectTimeout(requestTimeout);
        c.setReadTimeout(requestTimeout);

        return c;
    }
}