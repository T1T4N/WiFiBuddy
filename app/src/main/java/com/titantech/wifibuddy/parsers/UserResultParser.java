package com.titantech.wifibuddy.parsers;

import com.titantech.wifibuddy.models.User;

import org.json.JSONException;
import org.json.JSONObject;

public class UserResultParser implements ResultParser<User> {
    @Override
    public User parseResult(String content) throws JSONException {
        User ret = null;
        if (content == null) ret = User.nullUser();
        else if (content.equals("401")) {
            ret = User.genericUnauthorized();
        } else if (content.equals("404")) {
            ret = User.genericUnauthorized();
        } else if (content.equals("422")) {
            ret = User.genericExisting();
        } else if (content.length() > 3) {
            JSONObject jsonUser = new JSONObject(content);

            String id = jsonUser.getString("_id");
            String email = jsonUser.getString("email");
            String password = jsonUser.getString("password");

            ret = new User(id, email, password);
        }
        return ret;
    }
}
