package com.titantech.wifibuddy.parsers;

import org.json.JSONException;
import org.json.JSONObject;

public class AccessPointDeleteParser implements ResultParser<String> {
    @Override
    public String parseResult(String content) throws JSONException {
        if (content == null) {
            return String.valueOf(-2);  //Server unreachable
        } else if (content.equals("401")) {
            return String.valueOf(-1);  //Unauthorized or not owner
        } else {
            JSONObject jsonContent = new JSONObject(content);
            return jsonContent.getString("message");
        }
    }
}
