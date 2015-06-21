package de.goldenzweig.jimdostats;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mihahh on 20.06.2015.
 */
public class JimdoPerDayStatistics implements IJimdoPerDayStatistics {

    private int visits;
    private int pageViews;
    private Date date;

    public void setVisits(int visits) {
        this.visits = visits;
    }

    public void setPageViews(int pageViews) {
        this.pageViews = pageViews;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int getVisits() {
        return visits;
    }

    @Override
    public int getPageViews() {
        return pageViews;
    }

    @Override
    public String getDate() {
        return date.toString();
    }

    public String getShortDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM.dd");
        return formatter.format(date);
    }
}
