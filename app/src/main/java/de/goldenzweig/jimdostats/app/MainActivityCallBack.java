package de.goldenzweig.jimdostats.app;

/**
 * Created by mihahh on 18.12.2015.
 * Callback interface for the HTTPController to communicate back to the View(Activity)
 */
public interface MainActivityCallBack {

    void showRequestError(String errorMsg);
    void inflateAllCharts();
}
