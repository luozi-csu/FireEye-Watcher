package com.luozi.fireeyewatcher.model;

import org.json.JSONException;
import org.json.JSONObject;

public class JWTToken {
    public int uid;
    public String name;
    public String jwt;
    public String desc;

    public JWTToken(int uid, String name, String jwt, String desc) {
        this.uid = uid;
        this.name = name;
        this.jwt = jwt;
        this.desc = desc;
    }

    public static JWTToken parseFromJson(JSONObject json) throws JSONException {
        return new JWTToken(
                json.getInt("uid"),
                json.getString("name"),
                json.getString("jwt"),
                json.getString("desc")
        );
    }
}
