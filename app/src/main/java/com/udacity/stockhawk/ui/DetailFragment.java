package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.CustomMarkerView;
import com.udacity.stockhawk.utils.XAxisDateFormatter;
import com.udacity.stockhawk.utils.YAxisPriceFormatter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import timber.log.Timber;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final int STOCK_LOADER_ID = 2;

    // @formatter:off
    @State public String fragmentDataType;
    @State public String dateFormat;
    @State public int dataColumnPosition;
    @State public Uri stockUri;
    @State public String historyData;

    @BindView(R.id.stock_chart) public LineChart linechart;
    @BindColor(R.color.stock_chart_line_color) public int stockChartLineColor;
    // @formatter:on

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Icepick.restoreInstanceState(this, savedInstanceState);
        if (null == savedInstanceState)
        {
            fragmentDataType = getArguments().getString(getString(R.string.FRAGMENT_DATA_TYPE_KEY));
            dataColumnPosition = Contract.Quote.POSITION_HISTORY;
            if (fragmentDataType.equals(getString(R.string.MONTHLY)))
            {
                dateFormat = "MMM";
            }
            else if (fragmentDataType.equals(getString(R.string.WEEKLY)))
            {
                dateFormat = "dd";
            }
            else
            {
                dateFormat = "dd";
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);
        context = getContext();
        if (historyData != null)
            setupLineChart();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        stockUri = getActivity().getIntent().getData();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getLoaderManager().initLoader(STOCK_LOADER_ID, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        if (null != stockUri)
        {
            return new CursorLoader(
                    context,
                    stockUri,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[Contract.Quote.QUOTE_COLUMNS.size()]),
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if (data.moveToFirst() && null == historyData)
        {
            //set up the chart with history data
            historyData = data.getString(dataColumnPosition);
            setupLineChart();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
    }

    private void setupLineChart()
    {
        Pair<Float, List<Entry>> result = getFormattedStockHistory(historyData);
        List<Entry> dataPairs = result.second;
        Float referenceTime = result.first;
        LineDataSet dataSet = new LineDataSet(dataPairs, "");
        dataSet.setColor(stockChartLineColor);
        dataSet.setLineWidth(2f);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setCircleColor(stockChartLineColor);
        dataSet.setHighLightColor(stockChartLineColor);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        linechart.setData(lineData);

        XAxis xAxis = linechart.getXAxis();
        xAxis.setValueFormatter(new XAxisDateFormatter(dateFormat, referenceTime));
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(stockChartLineColor);
        xAxis.setAxisLineWidth(1.5f);
        xAxis.setTextColor(stockChartLineColor);
        xAxis.setTextSize(12f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = linechart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxis = linechart.getAxisLeft();
        yAxis.setValueFormatter(new YAxisPriceFormatter());
        yAxis.setDrawGridLines(false);
        yAxis.setAxisLineColor(stockChartLineColor);
        yAxis.setAxisLineWidth(1.5f);
        yAxis.setTextColor(stockChartLineColor);
        yAxis.setTextSize(12f);

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext(),
                R.layout.marker_view, getLastButOneData(dataPairs), referenceTime);


        Legend legend = linechart.getLegend();
        legend.setEnabled(false);

        linechart.setMarker(customMarkerView);

        //disable all interactions with the graph
        linechart.setDragEnabled(false);
        linechart.setScaleEnabled(false);
        linechart.setDragDecelerationEnabled(false);
        linechart.setPinchZoom(false);
        linechart.setDoubleTapToZoomEnabled(false);
        Description description = new Description();
        description.setText(" ");
        linechart.setDescription(description);
        linechart.setExtraOffsets(10, 0, 0, 10);
        linechart.animateX(1500, Easing.EasingOption.Linear);
    }

    private Entry getLastButOneData(List<Entry> dataPairs)
    {
        if (dataPairs.size() > 2)
        {
            return dataPairs.get(dataPairs.size() - 2);
        }
        else
        {
            return dataPairs.get(dataPairs.size() - 1);
        }
    }

    /**
     * @param history data retrieved from database
     *                example : 1480050000000:60.4453453$148005002340:61.442343453$
     * @return formatted {@link java.util.List} where key
     * is the date and value is the closing stock value
     */
    public static Pair<Float, List<Entry>> getFormattedStockHistory(String history)
    {
        List<Entry> entries = new ArrayList<>();
        List<Float> timeData = new ArrayList<>();
        List<Float> stockPrice = new ArrayList<>();

        CSVReader reader = new CSVReader(new StringReader(history));
        String [] entry;

        try
        {
            while (null != (entry = reader.readNext())) {
                // entry[] is an array of entry from the line
                timeData.add(Float.valueOf(entry[0]));
                stockPrice.add(Float.valueOf(entry[1]));
            }
        }
        catch (IOException e)
        {
            Timber.e("Could not parse history value: " + e.getLocalizedMessage());
        }

        Collections.reverse(timeData);
        Collections.reverse(stockPrice);
        Float referenceTime = timeData.get(0);
        for (int i = 0; i < timeData.size(); i++)
        {
            entries.add(new Entry(timeData.get(i) - referenceTime, stockPrice.get(i)));
        }
        return new Pair<>(referenceTime, entries);
    }
}
