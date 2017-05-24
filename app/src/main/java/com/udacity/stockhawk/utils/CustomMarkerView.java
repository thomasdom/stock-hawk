package com.udacity.stockhawk.utils;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.udacity.stockhawk.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomMarkerView extends MarkerView
{
    private final Date date;
    private final SimpleDateFormat dateFormat;
    private final TextView textView;
    private final Entry finalEntry;
    private final Float referenceTime;
    private final DecimalFormat dollarFormat;

    public CustomMarkerView(Context context, int layoutResource, Entry finalEntry, Float referenceTime)
    {
        super(context, layoutResource);
        textView = (TextView) findViewById(R.id.marker_text);
        dateFormat = new SimpleDateFormat("d MMM y", Locale.getDefault());
        date = new Date();
        this.finalEntry = finalEntry;
        this.referenceTime = referenceTime;
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight)
    {
        super.refreshContent(e, highlight);
        Float stockValue = e.getY();
        date.setTime((long) (e.getX() + referenceTime));
        String formattedDate = dateFormat.format(date);
        textView.setText(String.format(getContext().getString(R.string.marker_text), dollarFormat.format(stockValue), formattedDate));

        if (e.getX() >= finalEntry.getX())
        {
            setOffset(-256, -32);
        } else
        {
            setOffset(-128, -32);
        }
    }
}
