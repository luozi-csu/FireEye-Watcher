package com.luozi.fireeyewatcher.model;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    public int id;
    public String username;
    public String password;

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public static User parseFromJson(JSONObject json) throws JSONException {
        return new User(
                json.getInt("id"),
                json.getString("name"),
                ""
        );
    }
}
