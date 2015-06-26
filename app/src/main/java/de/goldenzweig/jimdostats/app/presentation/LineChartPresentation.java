/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats.app.presentation;

import java.util.Arrays;

public class LineChartPresentation {

    public float[] visitsArray;
    public float[] pageViewsArray;
    public String[] datesArray;

    public int maxValue;
    public int step;

    @Override
    public String toString() {
        return "LineChartPresentation{" +
                "visitsArray=" + Arrays.toString(visitsArray) +
                ", pageViewsArray=" + Arrays.toString(pageViewsArray) +
                ", datesArray=" + Arrays.toString(datesArray) +
                ", maxValue=" + maxValue +
                ", step=" + step +
                '}';
    }
}
