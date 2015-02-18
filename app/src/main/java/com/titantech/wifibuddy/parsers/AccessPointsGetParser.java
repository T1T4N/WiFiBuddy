package com.titantech.wifibuddy.parsers;

import com.titantech.wifibuddy.models.AccessPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccessPointsGetParser implements ResultParser<List<AccessPoint>> {
    private boolean mPopulated;

    public AccessPointsGetParser(boolean populated) {
        mPopulated = populated;
    }

    @Override
    public List<AccessPoint> parseResult(String content) throws JSONException {
        List<AccessPoint> ret = null;

        if (content == null)
            return Collections.emptyList(); //throw new JSONException("No content received in the parser");

        if (content.equals("401")) {
            ret = Collections.emptyList();
        } else if (content.length() > 3) {
            ret = new ArrayList<AccessPoint>();

            JSONArray arrItems = new JSONArray(content);
            for (int i = 0; i < arrItems.length(); i++) {
                JSONObject iter = (JSONObject) arrItems.get(i);

                String id = iter.getString("_id");
                String publisherId, publisherMail = null;

                if (mPopulated) {
                    JSONObject jPublisher = iter.getJSONObject("publisher");
                    publisherId = jPublisher.getString("_id");
                    publisherMail = jPublisher.getString("email");
                } else {
                    publisherId = iter.getString("publisher");
                }

                String bssid = iter.getString("bssid");
                String name = iter.getString("name");
                String password = iter.getString("password");
                String securityType = iter.getString("securityType");
                int privacyType = iter.getInt("privacyType");
                double lat = iter.getDouble("lat");
                double lon = iter.getDouble("lon");
                String lastAccessed = iter.getString("lastAccessed");

                AccessPoint ap = new AccessPoint(id, publisherId, publisherMail, bssid, name, password,
                    securityType, privacyType, lat, lon, lastAccessed);
                ret.add(ap);
            }
        }
        return ret;
    }
}
