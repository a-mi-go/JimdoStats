/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats.backend;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.goldenzweig.jimdostats.model.JimdoOneDayStatistics;
import de.goldenzweig.jimdostats.model.PageView;
import de.goldenzweig.jimdostats.model.Visit;

public class JimdoStatisticsMockDataProvider {

    private static final long MILLISECONDS_IN_DAY = 86400000;

    /**
     * Generates a list of random Jimdo Website statistics.
     *
     * @param numberOfDays Number of {@link JimdoOneDayStatistics} to be generated
     * @return generated list of {@link JimdoOneDayStatistics}
     */
    public static List<JimdoOneDayStatistics> generateMockStats(int numberOfDays) {

        List<JimdoOneDayStatistics> mockStats = new ArrayList<>();

        String[] referer = {"www.google.com", "www.yandex.ru", "www.altavista.com"};
        String[] pages = {"test1.jimdo.com", "test2.jimdo.com", "test3.jimdo.com"};
        String[] devices = {"android", "iphone", "pc"};
        String[] os = {"Windows", "Linux", "MacOS", "iOS", "Android"};

        Random rand = new Random();
        // Go back in time for the "numberOfDays"
        long dayTime = Calendar.getInstance().getTimeInMillis() - (MILLISECONDS_IN_DAY * numberOfDays);

        for (int day = 0; day < numberOfDays; day++) {

            JimdoOneDayStatistics dayStat = new JimdoOneDayStatistics();
            int uniqueVisits = rand.nextInt(20);
            int pageViews = rand.nextInt(3) + 1; //1 to 4 pageviews per visit

            for (int v = 0; v < uniqueVisits; v++) {

                Visit visit = new Visit();
                for (int p = 0; p < pageViews; p++) {

                    PageView pageView = new PageView();
                    pageView.setPage(pages[rand.nextInt(pages.length)]);
                    pageView.setTimeSpentOnPage(rand.nextInt(100000));
                    visit.addPageView(pageView);
                }
                visit.setReferer(referer[rand.nextInt(referer.length)]);
                visit.setDevice(devices[rand.nextInt(devices.length)]);
                visit.setOS(os[rand.nextInt(os.length)]);
                dayStat.addVisit(visit);
            }
            dayStat.setDate(new Date(dayTime));
            // Back to the future
            dayTime = dayTime + MILLISECONDS_IN_DAY;

            mockStats.add(dayStat);
        }

        return mockStats;
    }
}
