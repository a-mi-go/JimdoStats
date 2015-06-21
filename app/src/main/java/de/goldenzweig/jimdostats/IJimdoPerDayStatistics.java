package de.goldenzweig.jimdostats;

/**
 * Created by mihahh on 20.06.2015.
 */
public interface IJimdoPerDayStatistics {
    // Unique visit for a website on a given date
    int getVisits();

    // Page view for a website on a given date
    int getPageViews();

    String getDate();
}
