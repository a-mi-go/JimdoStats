/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.goldenzweig.jimdostats.backend.IJimdoPerDayStatistics;


public class JimdoOneDayStatistics implements IJimdoPerDayStatistics {

    private List<Visit> visits;
    private Date date;

    public JimdoOneDayStatistics() {
        visits = new ArrayList<>();
        date = new Date();
    }

    @Override
    public List<Visit> getVisits() {
        return visits;
    }

    @Override
    public int getVisitCount() {
        return visits.size();
    }

    @Override
    public int getPageViewCount() {
        int totalPageViews = 0;
        for (Visit v: visits) {
            totalPageViews += v.getPageViews().size();
        }
        return totalPageViews;
    }

    @Override
    public String getDate() {
        return date.toString();
    }

    @Override
    public String getShortDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM.dd");
        return formatter.format(date);
    }

    /**
     * Add a visit to to the day statistic
     * @param visit {@link Visit} instance to be added
     */
    public void addVisit(Visit visit) {
        this.visits.add(visit);
    }

    /**
     * @param date Date of the statistics
     */
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "JimdoOneDayStatistics{" +
                "visits=" + visits +
                ", date=" + date +
                '}';
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray jsonVisits = new JSONArray();
        for (Visit visit: visits) {
            jsonVisits.put(visit.toJSON());
        }
        json.put("visits", jsonVisits);
        json.put("date", getDate());
        return json;
    }
}
