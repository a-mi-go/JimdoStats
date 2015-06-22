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

    /**
     * @return Page views during the visit
     */
    public List<PageView> getPageViews() {
        return pageViews;
    }

    /**
     * @param pageViews List of Views to be set
     */
    public void setPageViews(List<PageView> pageViews) {
        this.pageViews = pageViews;
    }

    /**
     * @param pageView Page view to be added
     */
    public void addPageView(PageView pageView) {
        this.pageViews.add(pageView);
    }

    /**
     * @return Referer website
     */
    public String getReferer() {
        return referer;
    }

    /**
     * @param referer website
     */
    public void setReferer(String referer) {
        this.referer = referer;
    }

    /**
     * @return User agent information
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * @param userAgent User agent information
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * @return Device used for visiting the website
     */
    public String getDevice() {
        return device;
    }

    /**
     * @param device Device used for visiting the website
     */
    public void setDevice(String device) {
        this.device = device;
    }

    /**
     * @return OS running on device used for visiting the website
     */
    public String getOS() {
        return os;
    }

    /**
     * @param os OS running on device used for visiting the website
     */
    public void setOS(String os) {
        this.os = os;
    }
}
