/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats;

import java.util.Date;
import java.util.List;

public interface IJimdoPerDayStatistics {

    // Unique visit for a website on a given date
    List<Visit> getVisits();

    // Number of Unique visits for a website on a given date
    int getVisitCount();

    // Number of Page views for a website on a given date
    int getPageViewCount();

    // Date when the statistics have been made
    String getDate();

    // Date when the statistics have been made in short format
    String getShortDate();
}
