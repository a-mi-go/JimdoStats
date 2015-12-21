package de.goldenzweig.jimdostats.app;

/**
 * Created by mihahh on 18.12.2015.
 * Proxy class to ensure that the callback owner (MainActivity) is still alive (not null)
 */
public class MainActivityCallBackProxy implements MainActivityCallBack {

    private MainActivityCallBack callback;

    public MainActivityCallBackProxy(MainActivityCallBack mainActivityCallBack) {
        callback = mainActivityCallBack;
    }
    @Override
    public void showRequestError(String errorMsg) {
        if (callback != null) {
            callback.showRequestError(errorMsg);
        }
    }

    @Override
    public void inflateAllCharts() {
        if (callback != null) {
            callback.inflateAllCharts();
        }
    }
}
