/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats;

public class PageView {

    private String page;
    private long timeSpentOnPage;

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public long getTimeSpentOnPage() {
        return timeSpentOnPage;
    }

    public void setTimeSpentOnPage(long timeSpentOnPage) {
        this.timeSpentOnPage = timeSpentOnPage;
    }
}
