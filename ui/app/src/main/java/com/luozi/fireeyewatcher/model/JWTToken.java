package com.luozi.fireeyewatcher.model;

import org.json.JSONException;
import org.json.JSONObject;

public class JWTToken {
    public String jwt;
    public String desc;

    public JWTToken(String jwt, String desc) {
        this.jwt = jwt;
        this.desc = desc;
    }

    public static JWTToken parseFromJson(JSONObject json) throws JSONException {
        return new JWTToken(
                json.getString("jwt"),
                json.getString("desc")
        );
    }
}
