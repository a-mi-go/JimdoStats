/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.goldenzweig.jimdostats.model.Device;
import de.goldenzweig.jimdostats.model.Visit;
import de.goldenzweig.jimdostats.presentation.LineChartPresentation;
import de.goldenzweig.jimdostats.presentation.PieChartPresentation;


public class MainActivity extends AppCompatActivity {

    //Constants
    private static final int WEEK_DAYS = 7;
    private static final int MONTH_DAYS = 30;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    //Data
    private List<JimdoPerDayStatistics> mMockStats;
    private LineChartPresentation mWeekLineChartPresentation;
    private LineChartPresentation mMonthLineChartPresentation;
    private LineChartPresentation mCurrentLineChartPresentation;
    private PieChartPresentation mWeekDevicesPieChartPresentation;
    private PieChartPresentation mMonthDevicesPieChartPresentation;

    //Animation Style
    private BaseEasingMethod mCurrEasing = new QuintEaseOut();

    //Views
    private LineChartView mLineChart;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioWeek;
    private RadioButton mRadioMonth;
    private PopupWindow mPopupWindow;

    private Handler mHandler;

    /**
     * Inflates and redraws line chart in separate thread.
     */
    private final Runnable mInflateChartThread = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    inflateLineChart(mCurrentLineChartPresentation);
                }
            }, 100);
        }
    };

    /**
     * Enables radioGroup after chart animation is finished.
     */
    private final Runnable mAnimationEndAction = new Runnable() {
        @Override
        public void run() {
            setRadioGroupEnabled(true);
        }
    };

    /**
     * Enables or disables all radioButtons in the radio group.
     */
    private void setRadioGroupEnabled(boolean enabled) {
        if (mRadioGroup != null) {
            for (int i = 0; i < mRadioGroup.getChildCount(); i++) {
                mRadioGroup.getChildAt(i).setEnabled(enabled);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLineChart = (LineChartView) findViewById(R.id.linechart);
        mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
        mRadioWeek = (RadioButton) findViewById(R.id.radio_week);
        mRadioMonth = (RadioButton) findViewById(R.id.radio_month);
        mHandler = new Handler();

        //prepare the data here to avoid unnecessary overhead while switching views
        mMockStats = JimdoStatisticsMockDataProvider.generateMockStats(MONTH_DAYS);
        mWeekLineChartPresentation = prepareLineChartData(WEEK_DAYS);
        mMonthLineChartPresentation = prepareLineChartData(MONTH_DAYS);
        mCurrentLineChartPresentation = mWeekLineChartPresentation;

        mWeekDevicesPieChartPresentation = prepareDevicesPieChartData(WEEK_DAYS);
        mMonthDevicesPieChartPresentation = prepareDevicesPieChartData(MONTH_DAYS);

        //set fling listner on the line chart
        final GestureDetector gdt = new GestureDetector(this, new HorizontalFlingListner());
        final LineChartView lineChart = (LineChartView) findViewById(R.id.linechart);
        lineChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                gdt.onTouchEvent(event);
                return true;
            }
        });

        //inflate chart in onResume()
    }

    private class HorizontalFlingListner implements GestureDetector.OnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Right to left
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                if (mRadioMonth.isChecked() && mRadioMonth.isEnabled()) {
                    mRadioWeek.setChecked(true);
                    switchToWeekLineChartVew();

                }
                return false;
                // Left to right
            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                if (mRadioWeek.isChecked() && mRadioWeek.isEnabled()) {
                    mRadioMonth.setChecked(true);
                    switchToMonthLineChartVew();
                }
                return false;
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
        mCurrentLineChartPresentation = mMonthLineChartPresentation;
        switchLineChartView();
    }

    /**
     * Switch to week view on the line chart
     */
    private void switchToWeekLineChartVew() {
        mCurrentLineChartPresentation = mWeekLineChartPresentation;
        switchLineChartView();

    }

    /**
     * Switch view on the line chart
     */
    private void switchLineChartView() {
        setRadioGroupEnabled(false);
        inflateAllCharts();
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
     * Prepare data for the Line Chart.
     *
     * @param days Number of Jimdo day usage statistics
     * @return {@link LineChartPresentation} object for Line Chart initialization
     */
    private LineChartPresentation prepareLineChartData(int days) {

        LineChartPresentation lcp = new LineChartPresentation();

        //initialize the LineChartPresentation instance
        lcp.visitsArray = new float[days + 1];
        lcp.pageViewsArray = new float[days + 1];
        lcp.datesArray = new String[days + 1];
        //Max value of visits or page views in the stats
        lcp.maxValue = 0;

        //first row is empty to avoid points being drawn directly on the y-axis
        lcp.datesArray[0] = "";

        for (int i = 1; i <= days; i++) {
            setDayStatisticsToPresentation(lcp, days, i);
        }

        //Round maxValue up to the closest 10
        lcp.maxValue = (int) Math.ceil(lcp.maxValue / 10d) * 10;
        //Calculate step so that we always have exactly 10 segments on the y-axis
        lcp.step = (int) Math.floor(lcp.maxValue / 10);

        return lcp;
    }

    /**
     * Prepare data for the devices Pie Chart.
     *
     * @param days umber of Jimdo day usage statistics
     * @return {@link PieChartPresentation} object for Pie Chart initialization
     */
    private PieChartPresentation prepareDevicesPieChartData(int days) {

        //TODO: add more colors for more devices
        String[] colors = {"#FE6DA8", "#56B7F1", "#FED70E", "#CDA67F"};
        PieChartPresentation pieChartPresentation = new PieChartPresentation();

        int differentDevices = 0;
        int overallDevices = 0;

        //generate map of devices names on device model instances
        Map<String, Device> devicesMap = new HashMap<>();

        for (int day = 0; day < days; day++) {
            JimdoPerDayStatistics dayStat = mMockStats.get(day);
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
     * Sets data from mMockStats to the given {@link LineChartPresentation} instance
     * for a specified day.
     * used by {@link #prepareLineChartData} method.
     *
     * @param lcp  {@link LineChartPresentation} instance
     * @param days Overall days to be set in the lcp ({@link LineChartPresentation})
     * @param day  For which day should the data be set. Value of this param may vary from 1 to n.
     */
    private void setDayStatisticsToPresentation(LineChartPresentation lcp, int days, int day) {

        // retrieve the mock stats starting from index 0
        JimdoPerDayStatistics stat = mMockStats.get(day - 1);

        int visits = stat.getVisitCount();
        int pageViews = stat.getPageViewCount();

        // update the maxValue of the LineChartPresentation
        if (visits > lcp.maxValue || pageViews > lcp.maxValue) {
            lcp.maxValue = Math.max(visits, pageViews);
        }

        lcp.visitsArray[day] = visits;
        lcp.pageViewsArray[day] = pageViews;

        // in month view add only every 4th date to avoid overfilling thy x-axis
        if (days == WEEK_DAYS) {
            lcp.datesArray[day] = stat.getShortDate();
        } else if ((day % 4) == 0) {
            lcp.datesArray[day] = stat.getShortDate();
        } else {
            lcp.datesArray[day] = "";
        }
    }

    /**
     *  Inflates the Line Chart and every other chart that should be shown
     */
    private void inflateAllCharts() {
        mInflateChartThread.run();
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
        dataSet.setDots(true)
                .setDotsColor(this.getResources().getColor(R.color.line_visits_bg))
                .setDotsRadius(Tools.fromDpToPx(5))
                .setDotsStrokeThickness(Tools.fromDpToPx(2))
                .setDotsStrokeColor(this.getResources().getColor(R.color.line_visits))
                .setLineColor(this.getResources().getColor(R.color.line_visits))
                .setLineThickness(Tools.fromDpToPx(3))
                .beginAt(1).endAt(lcp.datesArray.length);

        mLineChart.addData(dataSet);

        //init and add "page views" line to the chart
        dataSet = new LineSet();
        dataSet.addPoints(lcp.datesArray, lcp.pageViewsArray);
        dataSet.setDots(true)
                .setDotsColor(this.getResources().getColor(R.color.line_pageviews_bg))
                .setDotsRadius(Tools.fromDpToPx(5))
                .setDotsStrokeThickness(Tools.fromDpToPx(2))
                .setDotsStrokeColor(this.getResources().getColor(R.color.line_pageviews))
                .setLineColor(this.getResources().getColor(R.color.line_pageviews))
                .setLineThickness(Tools.fromDpToPx(3))
                .beginAt(1).endAt(lcp.datesArray.length);

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

    @Override
    public void onResume() {
        super.onResume();
        inflateAllCharts();
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
        LayoutInflater layoutInflater
                = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

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
        int datesShownInLineChart = mCurrentLineChartPresentation.datesArray.length - 1;
        if (WEEK_DAYS == datesShownInLineChart) {
            pieChartPresentation = mWeekDevicesPieChartPresentation;
        } else if (MONTH_DAYS == datesShownInLineChart) {
            pieChartPresentation = mMonthDevicesPieChartPresentation;
        } else {
            pieChartPresentation = prepareDevicesPieChartData(datesShownInLineChart);
        }

        //Add slices to the pie chart and inflate the devices agenda
        for (String device : pieChartPresentation.devices.keySet()) {
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
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
