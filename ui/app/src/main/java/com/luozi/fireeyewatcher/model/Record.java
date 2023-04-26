package com.luozi.fireeyewatcher.model;

import com.luozi.fireeyewatcher.utils.DateUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Record {
    public int id;
    public int uid;
    public String requestTime;
    public String finishedTime;
    public int result;

    public Record(int id, int uid, String requestTime, String finishTime, int result) {
        this.id = id;
        this.uid = uid;
        this.requestTime = requestTime;
        this.finishedTime = finishTime;
        this.result = result;
    }

    public static Record parseFromJson(JSONObject json) throws JSONException {
        return new Record(
                json.getInt("id"),
                json.getInt("uid"),
                DateUtil.convert(json.getInt("request_time")),
                json.getInt("finished_time") == 0 ? "" : DateUtil.convert(json.getInt("finished_time")),
                json.getInt("result")
        );
    }
}
