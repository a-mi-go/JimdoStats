/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats.backend;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import de.goldenzweig.jimdostats.model.JimdoOneDayStatistics;
import fi.iki.elonen.NanoHTTPD;

public class JimdoStatsHTTPD extends NanoHTTPD {

    private static final int NETWORK_MAX_LATENCY = 5000;

    private List<JimdoOneDayStatistics> statistics;
    /**
     * Constructs a HTTP server on given port.
     */
    public JimdoStatsHTTPD()throws IOException {
        super(8080);

        statistics = JimdoStatisticsMockDataProvider.generateMockStats(30);
    }

    @Override
    public Response serve(IHTTPSession session) {

        JSONObject response = new JSONObject();
        try {
            if (session.getMethod().equals(Method.GET)) {
                if (session.getUri().equals("/statistics")) {

                    // Marshal Statistics into json format
                    JSONArray jsonStats = new JSONArray();
                    for (JimdoOneDayStatistics stat: statistics) {
                        jsonStats.put(stat.toJSON());
                    }
                    response.put("status", 200);
                    response.put("statistics", jsonStats);

                } else {
                    response.put("status", 400);
                    response.put("statusMessage", "Unknown uri: " + session.getUri());
                }
            } else {
                response.put("status", 405);
                response.put("statusMessage", "Method not allowed. Use HTTP GET.");
            }
        } catch (JSONException e1) {
            try {
                e1.printStackTrace();
                response.put("status", 500);
                response.put("statusMessage", "Internal Server error. Please contact our support team.");
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
        }

        // Simulate network latency
        try {
            Random rand = new Random();
            Thread.sleep((long)rand.nextInt(NETWORK_MAX_LATENCY));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Send response
        return new NanoHTTPD.Response(response.toString());
    }

}
