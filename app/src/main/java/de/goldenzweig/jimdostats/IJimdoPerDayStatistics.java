/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats;

public interface IJimdoPerDayStatistics {
    // Unique visit for a website on a given date
    int getVisits();

    // Page view for a website on a given date
    int getPageViews();

    String getDate();
}
