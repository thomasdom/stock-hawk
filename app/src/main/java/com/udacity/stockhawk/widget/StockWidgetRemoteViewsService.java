package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

public class StockWidgetRemoteViewsService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new RemoteViewsFactory()
        {
            private Cursor data = null;

            @Override
            public void onCreate()
            {
                // Nothing to do here
            }

            @Override
            public void onDataSetChanged()
            {
                if (null != data)
                {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Uri stockUri = Contract.Quote.URI;
                data = getContentResolver().query(stockUri,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[Contract.Quote.QUOTE_COLUMNS.size()]),
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy()
            {
                if (null != data)
                {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount()
            {
                return null == data ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position)
            {
                if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position))
                {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.stock_widget_list_item);

                // Fetch data
                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                String price = data.getString(Contract.Quote.POSITION_PRICE);
                String percentageChange = data.getString(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                views.setTextViewText(R.id.symbol, symbol);
                views.setTextViewText(R.id.price, price);
                views.setTextViewText(R.id.change, percentageChange);

                final Intent fillInIntent = new Intent();
                Uri stockUri = Contract.Quote.makeUriForStock(symbol);
                fillInIntent.setData(stockUri);
                views.setOnClickFillInIntent(R.id.stock_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView()
            {
                return new RemoteViews(getPackageName(), R.layout.stock_widget_list_item);
            }

            @Override
            public int getViewTypeCount()
            {
                return 1;
            }

            @Override
            public long getItemId(int position)
            {
                if (data.moveToPosition(position))
                    return data.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds()
            {
                return true;
            }
        };
    }
}
