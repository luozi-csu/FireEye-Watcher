package com.luozi.fireeyewatcher.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Statistics {

    public static class DailyFreq {
        public int date;
        public int freq;

        public DailyFreq(int date, int freq) {
            this.date = date;
            this.freq = freq;
        }

        public static DailyFreq parseFromJson(JSONObject json) throws JSONException {
            return new DailyFreq(
                    json.getInt("date"),
                    json.getInt("freq")
            );
        }
    }

    public int userId;
    public int overheatNum;
    public int normalNum;
    public int underheatNum;
    public int processingNum;
    public int abnormalNum;
    public double overheatRate;
    public double normalRate;
    public double underheatRate;
    public double abnormalRate;
    public List<DailyFreq> dailyFreqs;

    public Statistics(
            int userId, int overheatNum, int normalNum,
            int underheatNum, int processingNum, int abnormalNum,
            double overheatRate, double normalRate, double underheatRate,
            double abnormalRate, List<DailyFreq> dailyFreqs
    ) {
        this.userId = userId;
        this.overheatNum = overheatNum;
        this.normalNum = normalNum;
        this.underheatNum = underheatNum;
        this.processingNum = processingNum;
        this.abnormalNum = abnormalNum;
        this.overheatRate = overheatRate;
        this.normalRate = normalRate;
        this.underheatRate = underheatRate;
        this.abnormalRate = abnormalRate;
        this.dailyFreqs = dailyFreqs;
    }

    public static Statistics parseFromJson(JSONObject json) throws JSONException {
        int userId = json.getInt("uid");
        int overheatNum = json.getInt("overheat_num");
        int normalNum = json.getInt("normal_num");
        int underheatNum = json.getInt("underheat_num");
        int processingNum = json.getInt("processing_num");
        int abnormalNum = json.getInt("abnormal_num");
        double overheatRate = json.getDouble("overheat_rate");
        double normalRate = json.getDouble("normal_rate");
        double underheatRate = json.getDouble("underheat_rate");
        double abnormalRate = json.getDouble("abnormal_rate");

        JSONArray freqs = json.getJSONArray("daily_freqs");
        List<DailyFreq> dailyFreqs = new ArrayList<>();
        for (int i = 0; i < freqs.length(); i++) {
            DailyFreq dailyFreq = DailyFreq.parseFromJson(freqs.getJSONObject(i));
            dailyFreqs.add(dailyFreq);
        }

        return new Statistics(
                userId, overheatNum, normalNum, underheatNum,
                processingNum, abnormalNum, overheatRate,
                normalRate, underheatRate, abnormalRate,
                dailyFreqs
        );
    }
}
