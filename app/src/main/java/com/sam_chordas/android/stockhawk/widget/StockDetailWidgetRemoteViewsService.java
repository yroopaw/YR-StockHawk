package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;


/**
 * Created by yusuf on 30/04/16.
 */


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StockDetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = StockDetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] STOCKQUOTE_COLUMS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CHANGE,
            QuoteColumns.BIDPRICE,
            QuoteColumns.CREATED,
            QuoteColumns.ISUP,
            QuoteColumns.ISCURRENT
    };



    // these indices must match the projection
    private static final int INDEX__ID = 0;
    private static final int INDEX_SYMBOL = 1;
    private static final int INDEX_PERCENT_CHANGE = 2;
    private static final int INDEX_CHANGE = 3;
    private static final int INDEX_BIDPRICE = 4;
    private static final int INDEX_CREATED = 5;
    private static final int INDEX_ISUP = 6;
    private static final int INDEX_ISCURRENT = 7;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                //Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, //TableName
                        null, //Columns
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null,
                        null
                       );
                Log.v("WidgetList Data", "datasetDump = " + DatabaseUtils.dumpCursorToString(data));
                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);
                String stockSymbol = data.getString(INDEX_SYMBOL);
                views.setTextViewText(R.id.widget_stock_symbol, stockSymbol);
                views.setTextViewText(R.id.widget_bid_price, data.getString(INDEX_BIDPRICE));
                String priceChange = data.getString(INDEX_CHANGE);
                views.setTextViewText(R.id.widget_change, priceChange);
                String riseOrDrop = priceChange.substring(0,1);
                if(riseOrDrop.equals("+"))
                {
                    views.setInt(R.id.widget_change, "setBackgroundColor", Color.GREEN);
                } else
                {
                    views.setInt(R.id.widget_change, "setBackgroundColor", android.graphics.Color.RED);
                }


                //*******
                final Intent fillInIntent = new Intent();
                // Uri stockDetailUri = create uri for particular staock
                //  fillInIntent.setData(stockDetailUri);
                fillInIntent.putExtra("stock_symbol", stockSymbol);
                //fillInIntent.setData(QuoteProvider.Quotes.withSymbol(stockSymbol));
                Log.v(LOG_TAG, "Stock Symbolintent: " + stockSymbol);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;

            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                //zz        views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX__ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }


        };
    }
}
