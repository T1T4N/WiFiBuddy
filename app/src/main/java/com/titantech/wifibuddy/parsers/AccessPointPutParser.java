package com.titantech.wifibuddy.parsers;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Robert on 11.02.2015.
 */
public class AccessPointPutParser implements ResultParser<Integer> {
    @Override
    public Integer parseResult(String content) throws JSONException {
        if (content == null) {
            return -2;  //Network error somehow
        } else if (content.equals("401")) {
            return -1;  //Unauthorized or not owner
        } else {
            JSONObject jsonContent = new JSONObject(content);
            return jsonContent.getInt("message");
        }
    }
}
