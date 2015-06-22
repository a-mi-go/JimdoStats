/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats;

public class PageView {

    private String page;
    private long timeSpentOnPage;

    /**
     * @return Page that was viewed
     */
    public String getPage() {
        return page;
    }

    /**
     * @param page Page that was viewed
     */
    public void setPage(String page) {
        this.page = page;
    }

    /**
     * @return Time spent on Webpage during the view
     */
    public long getTimeSpentOnPage() {
        return timeSpentOnPage;
    }

    /**
     * @param timeSpentOnPage Time spent on Webpage during the view
     */
    public void setTimeSpentOnPage(long timeSpentOnPage) {
        this.timeSpentOnPage = timeSpentOnPage;
    }
}
