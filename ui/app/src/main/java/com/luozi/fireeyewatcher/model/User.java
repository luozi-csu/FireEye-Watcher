package com.luozi.fireeyewatcher.model;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    public String username;
    public String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static User parseFromJson(JSONObject json) throws JSONException {
        return new User(
                json.getString("name"),
                ""
        );
    }
}
