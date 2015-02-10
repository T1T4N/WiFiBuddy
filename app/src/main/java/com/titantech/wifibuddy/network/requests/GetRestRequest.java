package com.titantech.wifibuddy.network.requests;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Robert on 23.01.2015.
 */
public class GetRestRequest extends RestRequest {

    public GetRestRequest(String requestUrl, String authUsername, String authPassword) {
        this(requestUrl, authUsername, authPassword, 1000);
    }

    public GetRestRequest(String requestUrl, String authUsername, String authPassword, int timeout) {
        this.requestUrl = requestUrl;
        this.requestType = RequestType.GET;
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
