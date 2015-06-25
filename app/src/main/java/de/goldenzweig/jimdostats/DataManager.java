/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.goldenzweig.jimdostats.model.Device;
import de.goldenzweig.jimdostats.model.JimdoOneDayStatistics;
import de.goldenzweig.jimdostats.model.PageView;
import de.goldenzweig.jimdostats.model.Visit;
import de.goldenzweig.jimdostats.presentation.LineChartPresentation;
import de.goldenzweig.jimdostats.presentation.PieChartPresentation;

public class DataManager {

    //Data
    private List<JimdoOneDayStatistics> mJimdoStatistics;
    private LineChartPresentation mWeekLineChartPresentation;
    private LineChartPresentation mMonthLineChartPresentation;
    private LineChartPresentation mCurrentLineChartPresentation;
    private PieChartPresentation mWeekDevicesPieChartPresentation;
    private PieChartPresentation mMonthDevicesPieChartPresentation;

    private JSONObject mJsonResponse;

    public LineChartPresentation getCurrentLineChartPresentation() {
        return mCurrentLineChartPresentation;
    }
    public PieChartPresentation getWeekDevicesPieChartPresentation() {
        return mWeekDevicesPieChartPresentation;
    }
    public PieChartPresentation getMonthDevicesPieChartPresentation() {
        return mMonthDevicesPieChartPresentation;
    }
    public void setCurrentLineChartPresentationToMonth() {
        this.mCurrentLineChartPresentation = mMonthLineChartPresentation;
    }
    public void setCurrentLineChartPresentationToWeek() {
        this.mCurrentLineChartPresentation = mWeekLineChartPresentation;
    }
    public JSONObject getJsonStatistics() {
        return mJsonResponse;
    }

    /**
     * @return true if Jimdo statistics data is available.<br>
     *         false otherwise.
     */
    public boolean isDataAvailable() {
        return (mJimdoStatistics != null && !mJimdoStatistics.isEmpty());
    }

    /**
     * Recovers data from the bundle saved in onSaveInstanceState of the MainActivity
     * @param strJsonResponse json representation of a response from JimdoStats server
     */
    public void recoverData(String strJsonResponse) {
        try {
            unmarshalResponse(new JSONObject(strJsonResponse));
            prepareAllData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prepare 2 sets of data respectively to be shown in the line and pie charts.
     */
    public void prepareAllData() {
        //line chart data
        mWeekLineChartPresentation = prepareLineChartData(Constants.WEEK_DAYS);
        mMonthLineChartPresentation = prepareLineChartData(Constants.MONTH_DAYS);
        mCurrentLineChartPresentation = mWeekLineChartPresentation;
        //pie chart data
        mWeekDevicesPieChartPresentation = prepareDevicesPieChartData(Constants.WEEK_DAYS);
        mMonthDevicesPieChartPresentation = prepareDevicesPieChartData(Constants.MONTH_DAYS);
    }

    /**
     * Prepare data for the Line Chart.
     *
     * @param days Number of Jimdo day usage statistics
     * @return {@link LineChartPresentation} object for Line Chart initialization
     */
    private LineChartPresentation prepareLineChartData(int days) {

        LineChartPresentation lcp = new LineChartPresentation();

        // Initialize the LineChartPresentation instance
        lcp.visitsArray = new float[days + 1];
        lcp.pageViewsArray = new float[days + 1];
        lcp.datesArray = new String[days + 1];
        // Max value of visits or page views in the stats
        lcp.maxValue = 0;
        // First row is empty to avoid points being drawn directly on the y-axis
        lcp.datesArray[0] = "";

        // Show only the most recent statistics
        int beginDay = mJimdoStatistics.size() - days;
        for (int i = beginDay; i < mJimdoStatistics.size(); i++) {
            setDayStatisticsToPresentation(lcp, days, i);
        }

        // Round maxValue up to the closest 10
        lcp.maxValue = (int) Math.ceil(lcp.maxValue / 10d) * 10;
        // Calculate step so that we always have exactly 10 segments on the y-axis
        lcp.step = (int) Math.floor(lcp.maxValue / 10);

        return lcp;
    }

    /**
     * Sets data from mJimdoStatistics to the given {@link LineChartPresentation} instance
     * for a specified day.
     * used by {@link #prepareLineChartData} method.
     *
     * @param lcp  {@link LineChartPresentation} instance
     * @param days Overall days to be set in the lcp ({@link LineChartPresentation})
     * @param day  For which day should the data be set. Value must be in range [0 .. (days - 1)].
     */
    private void setDayStatisticsToPresentation(LineChartPresentation lcp, int days, int day) {

        // Retrieve staticss for the given day
        JimdoOneDayStatistics stat = mJimdoStatistics.get(day);

        int visits = stat.getVisitCount();
        int pageViews = stat.getPageViewCount();

        // Update the maxValue of the LineChartPresentation
        if (visits > lcp.maxValue || pageViews > lcp.maxValue) {
            lcp.maxValue = Math.max(visits, pageViews);
        }

        // index must be in range [1 .. day]
        int index = day - (mJimdoStatistics.size() - days) + 1;
        lcp.visitsArray[index] = visits;
        lcp.pageViewsArray[index] = pageViews;

        // In month view add only every 4th date to avoid overfilling thy x-axis
        if (days == Constants.WEEK_DAYS) {
            lcp.datesArray[index] = stat.getShortDate();
        } else if ((day % 4) == 0) {
            lcp.datesArray[index] = stat.getShortDate();
        } else {
            lcp.datesArray[index] = "";
        }
    }

    /**
     * Prepare data for the devices Pie Chart.
     *
     * @param days umber of Jimdo day usage statistics
     * @return {@link PieChartPresentation} object for Pie Chart initialization
     */
    private PieChartPresentation prepareDevicesPieChartData(int days) {

        //TODO: add more colors for more devices or generate a color pallete
        String[] colors = {"#FE6DA8", "#56B7F1", "#FED70E", "#CDA67F"};
        PieChartPresentation pieChartPresentation = new PieChartPresentation();

        int differentDevices = 0;
        int overallDevices = 0;

        //generate map of devices names on device model instances
        Map<String, Device> devicesMap = new HashMap<>();

        for (int day = 0; day < days; day++) {
            JimdoOneDayStatistics dayStat = mJimdoStatistics.get(day);
            for (Visit visit: dayStat.getVisits()) {
                String deviceName = visit.getDevice();
                if (devicesMap.containsKey(deviceName)) {
                    devicesMap.get(deviceName).incCount(); //count++
                    overallDevices++;
                } else {
                    overallDevices++;
                    Device device = new Device();
                    device.setName(deviceName);
                    device.setColor(colors[differentDevices++]);
                    device.setCount(1);
                    devicesMap.put(deviceName, device);
                }
            }
        }
        //calculate and set usage percent for each device
        for (String device : devicesMap.keySet()) {
            Device dp = devicesMap.get(device);
            dp.setPercent((dp.getCount() * 100) / overallDevices);
        }

        pieChartPresentation.devices = devicesMap;
        return pieChartPresentation;
    }

    /**
     * Parses json server response and converts them back into Java objects.
     *
     * @param response json representation of Jimdo website statistics
     * @throws JSONException
     */
    public void unmarshalResponse(JSONObject response) throws JSONException {

        // Save json representation of the response for future use
        mJsonResponse = response;
        mJimdoStatistics = new ArrayList<>();
        JSONArray jsonStatistics = response.getJSONArray("statistics");


        // Parse array of JimdoOneDayStatistics
        for (int s = 0; s < jsonStatistics.length(); s++) {
            JSONObject jsonOneDayStats = (JSONObject) jsonStatistics.get(s);
            JimdoOneDayStatistics oneDayStats = new JimdoOneDayStatistics();
            JSONArray jsonVisits = jsonOneDayStats.getJSONArray("visits");

            // Parse array of page Visits
            for (int v = 0; v < jsonVisits.length(); v++) {
                JSONObject jsonVisit = (JSONObject) jsonVisits.get(v);
                Visit visit = new Visit();
                JSONArray jsonPageViews = jsonVisit.getJSONArray("pageViews");

                // Parse array of PageViews
                for (int p = 0; p < jsonPageViews.length(); p++) {
                    JSONObject jsonPageView = (JSONObject) jsonPageViews.get(p);
                    PageView pageView = new PageView();
                    pageView.setPage(jsonPageView.getString("page"));
                    pageView.setTimeSpentOnPage(jsonPageView.getLong("timeSpentOnPage"));
                    visit.addPageView(pageView);
                }

                // Other Visit attributes
                visit.setDevice(jsonVisit.getString("device"));
                visit.setReferer(jsonVisit.getString("referer"));
                visit.setOS(jsonVisit.getString("os"));
                oneDayStats.addVisit(visit);
            }
            // Parse and recreate Date
            String date = jsonOneDayStats.getString("date");
            try {
                SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                oneDayStats.setDate(format.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mJimdoStatistics.add(oneDayStats);
        }
    }
}
