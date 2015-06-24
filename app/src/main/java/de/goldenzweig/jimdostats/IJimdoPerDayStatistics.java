/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats;

import java.util.List;
import de.goldenzweig.jimdostats.model.Visit;

interface IJimdoPerDayStatistics {

    /**
     * @return Unique {@link Visit}s for a website on a given date
     */
    List<Visit> getVisits();

    /**
     * @return Number of Unique {@link Visit}s for a website on a given date
     */
    int getVisitCount();

    /**
     * @return Number of Page views for a website on a given date
     */
    int getPageViewCount();

    /**
     * @return Date when the statistics have been made
     */
    String getDate();

    /**
     * @return Date when the statistics have been made in short format
     */
    String getShortDate();
}
