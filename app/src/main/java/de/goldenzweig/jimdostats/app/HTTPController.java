package de.goldenzweig.jimdostats.app;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Created by mihahh on 18.12.2015.
 * Handles HTTP requests to the Jimdo statistics server
 */
public class HTTPController {

    private static final int MY_SOCKET_TIMEOUT_MS = 10000;

    private static final String statisticsRequestDomain = "http://localhost";
    private static final String statisticsRequestPort = "8080";
    private static final String statisticRequestsPath = "/statistics";


    // Singleton
    private static volatile HTTPController instance = null;
    private HTTPController() {}
    public static synchronized HTTPController getInstance() {
        if (instance == null) {
            instance = new HTTPController();
        }
        return instance;
    }

    /**
     * @return full URL for Jimdo Statistics server request
     */
    private String buildStatisticsRequestURL() {
        return statisticsRequestDomain + ":" + statisticsRequestPort + statisticRequestsPath;
    }

    /**
     * Request Jimdo website usage statistics from server using google volley library.
     * Inflate and show data in the line chart.
     */
    public void requestJimdoPerDayStatistics(final MainActivityCallBack callBack) {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                buildStatisticsRequestURL(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                handleRequestJimdoPerDayStatisticsResponse(response, callBack);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.showRequestError(error.toString());
            }
        });

        // Set request timeout
        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add request to request queue
        AppController.getInstance().addToRequestQueue(request);
    }

    /**
     * Handle the response: unmarshal response, prepare data, inflate charts.
     * @param response JSON response
     */
    public void handleRequestJimdoPerDayStatisticsResponse(JSONObject response, MainActivityCallBack callBack) {
        String errorMsg = null;
        try {
            int status = response.getInt("status");
            if (status == HttpURLConnection.HTTP_OK) {
                DataManager.getInstance().unmarshalResponse(response);
                DataManager.getInstance().prepareAllData();
                callBack.inflateAllCharts();
            } else {
                errorMsg = "Error: " + response.getString("statusMessage");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            errorMsg = "Error: " + e.getMessage();
        }

        callBack.showRequestError(errorMsg);
    }
}
