/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats;

import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BaseEasingMethod;
import com.db.chart.view.animation.easing.quint.QuintEaseOut;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private static final int WEEK_DAYS = 7;
    private static final int MONTH_DAYS = 30;

    private List<JimdoPerDayStatistics> mockSatats;
    private LineChartPresentation weekLineChartPresentation;
    private LineChartPresentation monthLineChartPresentation;

    //Animation Style
    private static BaseEasingMethod mCurrEasing = new QuintEaseOut();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //prepare the data here to avoid unnecessary overhead while switching views
        mockSatats = generateMockStats(MONTH_DAYS);
        weekLineChartPresentation = prepareData(WEEK_DAYS);
        monthLineChartPresentation = prepareData(MONTH_DAYS);

        inflateChart(weekLineChartPresentation);
    }

    /**
     * RadioButton Group onClick listener.
     * Inflates the Line chart respectfully to the clicked RadioButton
     * @param view - clicked RadioButton
     */
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_week:
                if (checked)
                    inflateChart(weekLineChartPresentation);
                    break;
            case R.id.radio_month:
                if (checked)
                    inflateChart(monthLineChartPresentation);
                    break;
        }
    }

    /**
     * Prepare data for the Line Chart
     * @param days - Number of Jimdo day usage statistics
     * @return LineChartPresentation object for Line Chart initialization
     */
    private LineChartPresentation prepareData(int days) {

        LineChartPresentation lcp = new LineChartPresentation();
        //Max value of visits or page views in the stats
        int maxValue = 0;

        LinkedList<String> datesList = new LinkedList<>();
        LinkedList<Float> visitsList = new LinkedList<>();
        LinkedList<Float> pageViewsList = new LinkedList<>();

        for (int i = 0; i < days; i++) {
            JimdoPerDayStatistics stat = mockSatats.get(i);

            int visits = stat.getVisits();
            int pageViews = stat.getPageViews();

            if (visits > maxValue || pageViews > maxValue) {
                maxValue = Math.max(visits, pageViews);
            }

            visitsList.push((float)visits);
            pageViewsList.push((float)pageViews);

            // in month view add only every 4th date to avoid overfilling thy x-axis
            if (days == WEEK_DAYS) {
                datesList.push(stat.getShortDate());
            } else if ((i % 4) == 0) {
                datesList.push(stat.getShortDate());
            } else {
                datesList.push("");
            }
        }

        //first column is empty to avoid points being drawn directly on the y-axis
        visitsList.push(0f);
        pageViewsList.push(0f);
        datesList.push("");

        //Round maxValue up to the closest 10
        lcp.maxValue = (int) Math.ceil(maxValue / 10d) * 10;
        //Calculate step so that we always have exactly 10 segments on the y-axis
        lcp.step = (int) Math.floor(lcp.maxValue / 10);

        lcp.visitsArray = new float[visitsList.size()];
        lcp.pageViewsArray = new float[pageViewsList.size()];
        lcp.datesArray = datesList.toArray(new String[datesList.size()]);

        //Line Charts accepts only primitive value arrays
        for (int i = 0; i <= days; i++) {
            lcp.visitsArray[i] = visitsList.get(i);
            lcp.pageViewsArray[i] = pageViewsList.get(i);
        }

        return lcp;
    }


    /**
     * Inflates Line Chart with data, initializes the look and feel, shows the Line Chart.
     * @param lcp - LineChartPresentation instance.
     */
    private void inflateChart(LineChartPresentation lcp) {

        LineChartView mLineChart = (LineChartView) findViewById(R.id.linechart);
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

        mLineChart.show(new Animation().setEasing(mCurrEasing));
    }


    /**
     * Generates a list with random Jimdo Website statistics.
     * @param numberOfDays - Number of Jimdo day usage statistics
     * @return generated list of JimdoPerDayStatistics
     */
    private List<JimdoPerDayStatistics> generateMockStats(int numberOfDays) {

        List<JimdoPerDayStatistics> mockStats = new ArrayList<>();

        Random rand = new Random();
        long dayTime = Calendar.getInstance().getTimeInMillis();
        for (int day = 0; day < numberOfDays; day++) {
            JimdoPerDayStatistics dayStat = new JimdoPerDayStatistics();
            int uniqueVisits = rand.nextInt(20);
            dayStat.setVisits(uniqueVisits);
            dayStat.setPageViews(uniqueVisits + rand.nextInt(20));
            dayTime = dayTime - (24*60*60*1000);
            dayStat.setDate(new Date(dayTime));

            mockStats.add(dayStat);
        }

        return mockStats;
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
