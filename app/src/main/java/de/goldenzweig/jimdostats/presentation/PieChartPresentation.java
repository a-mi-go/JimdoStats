/*
 * Copyright (C) 2015 Mikhail Goldenzweig
 * MIT Licence
 */
package de.goldenzweig.jimdostats.presentation;

import java.util.Map;
import de.goldenzweig.jimdostats.model.Device;

public class PieChartPresentation {

    public Map<String, Device> devices;

    @Override
    public String toString() {
        return "PieChartPresentation{" +
                "devices=" + devices +
                '}';
    }
}
