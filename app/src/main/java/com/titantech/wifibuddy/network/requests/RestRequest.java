package com.titantech.wifibuddy.network.requests;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public abstract class RestRequest {
    public static class RequestType {
        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";
    }

    //protected static SSLSocketFactory debug_socketFactory = RestRequest.debug_setUpHttpsConnection();
    protected String requestUrl;
    protected String requestType;
    protected String requestData;
    protected int requestTimeout;
    protected String encodedAuthorization;

    protected abstract HttpURLConnection setupConnection() throws IOException;

    protected String encodeCredentials(String username, String password) {
        if ((username == null || username.equals("")) && (password == null || password.equals(""))) {
            return "";
        }
        String formattedCredentials = username + ":" + password;
        try {
            byte[] formattedBytes = formattedCredentials.getBytes("UTF-8");
            return Base64.encodeToString(formattedBytes, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            Log.e("CONNECTION_ERROR", e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    public String getJson() {
        try {
            HttpURLConnection conn = setupConnection();
            // conn.setSSLSocketFactory(debug_socketFactory);
            conn.connect();
            int status = conn.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    return sb.toString();
                case 401: // Unauthorized
                    return "401";
                case 404: // Entity not found
                    return "404";
                case 422: // Validation Error
                    return "422";
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("NETWORK_ERROR", e.getMessage(), e);

            e.printStackTrace();
        }
        return null;
    }

    /*
    private static SSLSocketFactory debug_setUpHttpsConnection()
    {
        try
        {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            InputStream caInput = new BufferedInputStream(MainActivity.debug_context.getAssets().open("server.crt"));
            Certificate ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, tmf.getTrustManagers(), null);

            return context.getSocketFactory();
        }
        catch (Exception ex)
        {
            Log.e("HTTPS_ERROR", "Failed to establish SSL connection to server: " + ex.toString());
            return null;
        }
    }
    */
}