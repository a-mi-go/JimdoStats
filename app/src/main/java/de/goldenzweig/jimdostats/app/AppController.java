/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats.app;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.IOException;

import de.goldenzweig.jimdostats.backend.JimdoStatsHTTPD;

public class AppController extends Application {

    private final String TAG = AppController.class.getSimpleName();
    private RequestQueue mRequestQueue;
    private static AppController mInstance;
    private static JimdoStatsHTTPD mStatsHTTPD;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        //start HTTP Server
        try {
            mStatsHTTPD = new JimdoStatsHTTPD();
            mStatsHTTPD.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    /**
     * @return volley RequestQueue
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    /**
     *
     * @param req The request to service
     * @param tag Tag set to the request
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    /**
     * @param req The request to service
     */
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    /**
     * @param tag Tag of the request that should be canceled
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
