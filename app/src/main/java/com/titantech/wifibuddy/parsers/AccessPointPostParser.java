package com.titantech.wifibuddy.parsers;

import com.titantech.wifibuddy.models.AccessPoint;

import org.json.JSONException;
import org.json.JSONObject;

public class AccessPointPostParser implements ResultParser<AccessPoint> {

    @Override
    public AccessPoint parseResult(String content) throws JSONException {
        AccessPoint ret = null;

        if (content == null) {
            ret = null;    //throw new JSONException("No content received in the parser");
        } else if (content.equals("401")) {
            ret = null;     //throw new JSONException("Unauthorized");
        } else if (content.length() > 3) {
            JSONObject jsonObject = new JSONObject(content);

            String id = jsonObject.getString("_id");
            String publisherId = jsonObject.getString("publisher");

            String bssid = jsonObject.getString("bssid");
            String name = jsonObject.getString("name");
            String password = jsonObject.getString("password");
            String securityType = jsonObject.getString("securityType");
            int privacyType = jsonObject.getInt("privacyType");
            double lat = jsonObject.getDouble("lat");
            double lon = jsonObject.getDouble("lon");
            String lastAccessed = jsonObject.getString("lastAccessed");

            ret = new AccessPoint(id, publisherId, bssid, name, password,
                    securityType, privacyType, lat, lon, lastAccessed);
        }
        return ret;
    }
}
