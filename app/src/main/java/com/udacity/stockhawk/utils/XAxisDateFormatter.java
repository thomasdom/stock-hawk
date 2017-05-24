package com.udacity.stockhawk.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class XAxisDateFormatter implements IAxisValueFormatter
{
    private final SimpleDateFormat dateFormatter;
    private final Date date;
    private final Float referenceTime;

    public XAxisDateFormatter(String dateFormat, Float referenceTime)
    {
        this.dateFormatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
        this.date = new Date();
        this.referenceTime = referenceTime;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis)
    {
        date.setTime((long) (value + referenceTime));
        return dateFormatter.format(date);
    }
}
