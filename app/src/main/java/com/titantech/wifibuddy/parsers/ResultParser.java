package com.titantech.wifibuddy.parsers;

import org.json.JSONException;

public interface ResultParser<T> {
    public T parseResult(String content) throws JSONException;
}
