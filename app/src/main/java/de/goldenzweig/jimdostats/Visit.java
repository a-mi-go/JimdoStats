/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats;

import java.util.ArrayList;
import java.util.List;

public class Visit {

    private List<PageView> pageViews;

    private String referer;
    private String userAgent;
    private String device;
    private String os;

    public Visit() {
        pageViews = new ArrayList<>();
    }

    public List<PageView> getPageViews() {
        return pageViews;
    }

    public void setPageViews(List<PageView> pageViews) {
        this.pageViews = pageViews;
    }

    public void addPageView(PageView pageView) {
        this.pageViews.add(pageView);
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getOS() {
        return os;
    }

    public void setOS(String os) {
        this.os = os;
    }
}
