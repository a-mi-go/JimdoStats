/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Visit {

    private List<PageView> pageViews;

    private String referer;
    private String device;
    private String os;

    public Visit() {
        pageViews = new ArrayList<>();
    }

    /**
     * @return {@link PageView}s of the visit
     */
    public List<PageView> getPageViews() {
        return pageViews;
    }

    /**
     * @param pageViews List of {@link PageView}s to be set
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

    @Override
    public String toString() {
        return "Visit{" +
                "pageViews=" + pageViews +
                ", referer='" + referer + '\'' +
                ", device='" + device + '\'' +
                ", os='" + os + '\'' +
                '}';
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray jsonPageViews = new JSONArray();
        for (PageView pageView: pageViews) {
            jsonPageViews.put(pageView.toJSON());
        }
        json.put("pageViews", jsonPageViews);
        json.put("referer", referer);
        json.put("device", device);
        json.put("os", os);
        return json;
    }
}
