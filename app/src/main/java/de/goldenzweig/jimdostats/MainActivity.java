/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats;

import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BaseEasingMethod;
import com.db.chart.view.animation.easing.quint.QuintEaseOut;

import java.text.DecimalFormat;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int WEEK_DAYS = 7;
    private static final int MONTH_DAYS = 30;

    private List<JimdoPerDayStatistics> mMockStats;
    private LineChartPresentation mWeekLineChartPresentation;
    private LineChartPresentation mMonthLineChartPresentation;
    private LineChartPresentation mCurrentChartPresentation;

    //Animation Style
    private BaseEasingMethod mCurrEasing = new QuintEaseOut();

    private LineChartView mLineChart;
    private RadioGroup mRadioGroup;
    private Handler mHandler;

    /**
     * Inflates and redraws line chart in separate thread.
     */
    private final Runnable mInflateChartThread = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    inflateLineChart(mCurrentChartPresentation);
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
        mHandler = new Handler();

        //prepare the data here to avoid unnecessary overhead while switching views
        mMockStats = JimdoStatisticsMockDataProvider.generateMockStats(MONTH_DAYS);
        mWeekLineChartPresentation = prepareLineChartData(WEEK_DAYS);
        mMonthLineChartPresentation = prepareLineChartData(MONTH_DAYS);

        inflateLineChart(mWeekLineChartPresentation);
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
                    mCurrentChartPresentation = mWeekLineChartPresentation;
                    break;
                case R.id.radio_month:
                    mCurrentChartPresentation = mMonthLineChartPresentation;
                    break;
            }
        }
        setRadioGroupEnabled(false);
        mInflateChartThread.run();
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
        lcp.visitsArray[0] = 0f;
        lcp.pageViewsArray[0] = 0f;
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
     * Sets data from mMockStats to the given {@link LineChartPresentation} instance
     * for a specified day.
     * used by {@link #prepareLineChartData} method.
     *
     * @param lcp {@link LineChartPresentation} instance
     * @param days Overall days to be set in the lcp ({@link LineChartPresentation})
     * @param day For which day should the data be set. Value of this param may vary from 1 to n.
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
     * TODO: show top devices (for e.g. as pie chart) used for viewing pages
     */
    public void onTopDevicesButtonClicked(View view) {
        Toast toast = Toast.makeText(getApplicationContext(),
                "This feature is not implemented yet",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
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
