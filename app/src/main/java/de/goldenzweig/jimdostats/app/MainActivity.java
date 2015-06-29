/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats.app;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BaseEasingMethod;
import com.db.chart.view.animation.easing.quint.QuintEaseOut;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.DecimalFormat;

import de.goldenzweig.jimdostats.R;
import de.goldenzweig.jimdostats.app.presentation.LineChartPresentation;
import de.goldenzweig.jimdostats.app.presentation.PieChartPresentation;
import de.goldenzweig.jimdostats.model.Device;


public class MainActivity extends AppCompatActivity {

    //Constants
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int MY_SOCKET_TIMEOUT_MS = 10000;

    private static final String statisticsRequestDomain = "http://localhost";
    private static final String statisticsRequestPort = "8080";
    private static final String statisticRequestsPath = "/statistics";

    //Animation Style
    private BaseEasingMethod mCurrEasing = new QuintEaseOut();

    //Views
    private LineChartView mLineChart;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioWeek;
    private RadioButton mRadioMonth;
    private PopupWindow mPopupWindow;
    private ProgressDialog mProgressDialog;

    private DataManager mDataManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataManager = new DataManager();

        //initialize UI
        mLineChart = (LineChartView) findViewById(R.id.linechart);
        mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
        mRadioWeek = (RadioButton) findViewById(R.id.radio_week);
        mRadioMonth = (RadioButton) findViewById(R.id.radio_month);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);

        // if onCreate called for the first time
        if (savedInstanceState == null) {
            // request monthly statistics from server
            requestJimdoPerDayStatistics();
        }

        setLineChartFlingListner();
    }

    /**
     * @return full URL for Jimdo Statistics server request
     */
    private String buildStatisticsRequestURL() {
        return statisticsRequestDomain + ":" + statisticsRequestPort + statisticRequestsPath;
    }

    /**
     * Action performed after the Line Chart animation is finished.
     */
    private final Runnable mAnimationEndAction = new Runnable() {
        @Override
        public void run() {
            setLineChartUIControllsEnabled(true);
        }
    };

    /**
     * Set the enable state of the radio group.
     */
    private void setRadioGroupEnabled(boolean enabled) {
        if (mRadioGroup != null) {
            for (int i = 0; i < mRadioGroup.getChildCount(); i++) {
                mRadioGroup.getChildAt(i).setEnabled(enabled);
            }
        }
    }

    /**
     * Set the enable state of all UI that controls the Line Chart.
     * @param enabled True if the UI should be enabled, false otherwise
     */
    private void setLineChartUIControllsEnabled(boolean enabled) {
        setRadioGroupEnabled(enabled);
        mLineChart.setEnabled(enabled);

// TODO: instead of disabling the UI try to dismiss the prev animation
//        if (enabled == false) {
//            mLineChart.clearAnimation();
//            mLineChart.reset();
//        }
    }

    /**
     * Request Jimdo website usage statistics from server using google volley library.
     * Inflate and show data in the line chart.
     */
    private void requestJimdoPerDayStatistics() {

        showProgressDialog();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                buildStatisticsRequestURL(), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleRequestJimdoPerDayStatisticsResponse(response);
                    }
                }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(),
                                    error.getMessage(), Toast.LENGTH_SHORT).show();
                            hideProgressDialog();
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
    private void handleRequestJimdoPerDayStatisticsResponse(JSONObject response) {
        try {
            int status = response.getInt("status");
            if (status == HttpURLConnection.HTTP_OK) {
                mDataManager.unmarshalResponse(response);
            } else {
                Toast.makeText(getApplicationContext(),
                        "Error: " + response.getString("statusMessage"),
                        Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        mDataManager.prepareAllData();
        inflateAllCharts();
        hideProgressDialog();
    }

    /**
     * Show the progress dialog while the request is running.
     */
    private void showProgressDialog() {
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    /**
     * Hide the progress dialog on response.
     */
    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    /**
     * set fling listner on the line chart
     */
    private void setLineChartFlingListner() {
        final GestureDetector gdt = new GestureDetector(this, new HorizontalFlingListner());
        mLineChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                gdt.onTouchEvent(event);
                return true;
            }
        });
    }

    /**
     * Fling gesture listener for the line chart
     */
    private class HorizontalFlingListner implements GestureDetector.OnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Fling right to left or left to right
            if(Math.abs(e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                switchRadioButtonsAndLineChartView();
            }
            return false;
        }
        //Methods not used but have to be implemented
        @Override public boolean onDown(MotionEvent e) { return false; }
        @Override public void onShowPress(MotionEvent e) {}
        @Override public boolean onSingleTapUp(MotionEvent e) { return false; }
        @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
        @Override public void onLongPress(MotionEvent e) {}
    }

    /**
     * Switch to month view on the line chart
     */
    private void switchToMonthLineChartVew() {
        setLineChartUIControllsEnabled(false);
        mDataManager.setCurrentLineChartPresentationToMonth();
        inflateAllCharts();
    }

    /**
     * Switch to week view on the line chart
     */
    private void switchToWeekLineChartVew() {
        setLineChartUIControllsEnabled(false);
        mDataManager.setCurrentLineChartPresentationToWeek();
        inflateAllCharts();
    }

    /**
     * Switch view on the line chart
     */
    private void switchRadioButtonsAndLineChartView() {

        if (mRadioWeek.isChecked()) {
            mRadioMonth.setChecked(true);
            switchToMonthLineChartVew();
        } else {
            mRadioWeek.setChecked(true);
            switchToWeekLineChartVew();
        }
    }

    /**
     * RadioButton Group onClick listener.
     * Inflates the Line chart respectfully to the clicked RadioButton.
     *
     * @param view Clicked RadioButton
     */
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        if (checked) {
            switch (view.getId()) {
                case R.id.radio_week:
                    switchToWeekLineChartVew();
                    break;
                case R.id.radio_month:
                    switchToMonthLineChartVew();
                    break;
            }
        }
    }

    /**
     *  Inflates the Line Chart and every other chart that should be shown
     */
    private void inflateAllCharts() {
        inflateLineChart(mDataManager.getCurrentLineChartPresentation());
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            inflateDevicesPieChartPopup();
        }
    }

    /**
     * Inflates Line Chart with data, initializes the look and feel, shows the Line Chart.
     *
     * @param lcp {@link LineChartPresentation} instance
     */
    private void inflateLineChart(LineChartPresentation lcp) {

        mLineChart.reset();

        //init and add "visits" line to the chart
        LineSet dataSet = new LineSet();
        dataSet.addPoints(lcp.datesArray, lcp.visitsArray);
        setDotsAndLinesOptions(dataSet, R.color.line_visits_bg, R.color.line_visits, lcp.datesArray.length);
        mLineChart.addData(dataSet);

        //init and add "page views" line to the chart
        dataSet = new LineSet();
        dataSet.addPoints(lcp.datesArray, lcp.pageViewsArray);
        setDotsAndLinesOptions(dataSet, R.color.line_pageviews_bg, R.color.line_pageviews, lcp.datesArray.length);
        mLineChart.addData(dataSet);

        //general initialization
        mLineChart.setAxisBorderValues(0, lcp.maxValue, lcp.step);
        Paint mLineGridPaint = new Paint();
        mLineGridPaint.setColor(this.getResources().getColor(R.color.line_grid));
        mLineGridPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
        mLineGridPaint.setStyle(Paint.Style.STROKE);
        mLineGridPaint.setAntiAlias(true);
        mLineGridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));

        mLineChart.setBorderSpacing(Tools.fromDpToPx(4))
                .setGrid(LineChartView.GridType.HORIZONTAL, mLineGridPaint)
                .setXAxis(false)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYAxis(false)
                .setYLabels(YController.LabelPosition.OUTSIDE)
                .setLabelsFormat(new DecimalFormat("##"));

        Animation animation = new Animation().setEasing(mCurrEasing);
        mLineChart.show(animation.setEndAction(mAnimationEndAction));
    }

    /**
     * Set options for a line to be drawn in the line chart.
     * @param dataSet Line model
     * @param dotsColorId Color of the dots on the line
     * @param lineColorId Color of the line
     * @param end index of the last point to be drawn
     */
    private void setDotsAndLinesOptions(LineSet dataSet, int dotsColorId, int lineColorId, int end) {
        dataSet.setDots(true)
                .setDotsColor(this.getResources().getColor(dotsColorId))
                .setDotsRadius(Tools.fromDpToPx(5))
                .setDotsStrokeThickness(Tools.fromDpToPx(2))
                .setDotsStrokeColor(this.getResources().getColor(lineColorId))
                .setLineColor(this.getResources().getColor(lineColorId))
                .setLineThickness(Tools.fromDpToPx(3))
                .beginAt(1).endAt(end);
    }

    @Override
    public void onResume() {
        super.onResume();
        //redraw charts if Activity gets destroyed
        if (mDataManager.isDataAvailable()) {
            inflateAllCharts();
        }
    }

    /**
     * TODO: show top viewed pages for selected time period in a separate activity or popup window
     */
    public void onTopPagesButtonClicked(View view) {
        Toast toast = Toast.makeText(getApplicationContext(),
                "This feature is not implemented yet",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * TODO: show top referer pages for selected time period in a separate activity or popup window
     */
    public void onTopRefererButtonClicked(View view) {
        Toast toast = Toast.makeText(getApplicationContext(),
                "This feature is not implemented yet",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     *  Shows devices statistics with a Pie Chart in a popup window.
     */
    public void onTopDevicesButtonClicked(View view) {

        //create and show a popup window
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.devices_pie_chart, null);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        MainActivity.this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mPopupWindow = new PopupWindow(
                popupView,
                displayMetrics.widthPixels - 10,
                displayMetrics.heightPixels - 130);

        LinearLayout mainLayout = new LinearLayout(this);
        mPopupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);

        inflateDevicesPieChartPopup();
    }

    /**
     * Inflates Pie Chart Popup window with devices usage statistics.
     */
    private void inflateDevicesPieChartPopup() {

        View popupView = mPopupWindow.getContentView();
        PieChart pieChart = (PieChart) popupView.findViewById(R.id.pie_chart);
        LinearLayout devicesAgenda = (LinearLayout) popupView.findViewById(R.id.devices_agenda);

        //cleanups
        pieChart.clearChart();
        devicesAgenda.removeAllViews();

        //layout for the TextViews in agenda
        LayoutParams lparams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        //Choose devicePieChartPresentation
        PieChartPresentation pieChartPresentation;
        int datesShownInLineChart = mDataManager.getCurrentLineChartPresentation().datesArray.length - 1;
        if (Constants.WEEK_DAYS == datesShownInLineChart) {
            pieChartPresentation = mDataManager.getWeekDevicesPieChartPresentation();
        } else { //Constants.MONTH_DAYS == datesShownInLineChart
            pieChartPresentation = mDataManager.getMonthDevicesPieChartPresentation();
        }

        //Add slices to the pie chart and inflate the devices agenda
        for (String device: pieChartPresentation.devices.keySet()) {
            Device dp = pieChartPresentation.devices.get(device);
            pieChart.addPieSlice(
                    new PieModel(device, dp.getCount(), Color.parseColor(dp.getColor())));

            TextView tv = new TextView(this);
            tv.setLayoutParams(lparams);
            String text = "<font color=" + dp.getColor() + ">\u25A0</font> " +
                    "<font color=#dddddd>" + device + " - " + dp.getCount() +
                    " (" + dp.getPercent() + "%)" + "</font>";
            tv.setText(Html.fromHtml(text));
            devicesAgenda.addView(tv);
        }

        pieChart.startAnimation();
    }

    @Override
    public void onBackPressed() {
        // if popup window is showing, only dismiss the popup window.
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        } else {
        /*
         * Move the task containing this activity to the back of the activity stack
         * rather than calling super.onBackPressed() which destroys current activity.
         * This way data is saved in onSaveInstanceState(Bundle bundle)
         * and no extra request to server is needed
         */
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        // save json response to avoid server requests
        if (mDataManager.isDataAvailable()) {
            bundle.putString("jsonResponse", mDataManager.getJsonStatistics().toString());
        }
        // if devices popup is showing dismiss it and save its state
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            bundle.putBoolean("popupShowing", true);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDataManager.recoverData(savedInstanceState.getString("jsonResponse"));

        // if devices popup was showing before activity was killed, show it again after activity is created again
        if (savedInstanceState.getBoolean("popupShowing")) {
            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = layoutInflater.inflate(R.layout.devices_pie_chart, null);
            popupView.post(new Runnable() {
                public void run() {
                    onTopDevicesButtonClicked(null);
                }
            });
        }
    }

}
