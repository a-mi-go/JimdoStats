/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JimdoPerDayStatistics implements IJimdoPerDayStatistics {

    private List<Visit> visits;
    private Date date;

    public JimdoPerDayStatistics() {
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

    public void addVisit(Visit visit) {
        this.visits.add(visit);
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
