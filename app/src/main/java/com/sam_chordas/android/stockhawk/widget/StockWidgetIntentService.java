package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteDatabase;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

import net.simonvt.schematic.annotation.NotNull;

/**
 * Created by yusuf on 09/04/16.
 */
public class StockWidgetIntentService extends IntentService {

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

    private static final int INDEX__ID = 0;
    private static final int INDEX_SYMBOL = 1;
    private static final int INDEX_PERCENT_CHANGE = 2;
    private static final int INDEX_CHANGE = 3;
    private static final int INDEX_BIDPRICE = 4;
    private static final int INDEX_CREATED = 5;
    private static final int INDEX_ISUP = 6;
    private static final int INDEX_ISCURRENT = 7;


    public StockWidgetIntentService() {
        super(("StockWidgetIntentService"));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Retreive all of Stock widget ids: these are widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds((new ComponentName(this,
                StockQuoteWidgetProvider.class)));

        //Get Stock quote from Content Provider

        //TODO Uri stockQuoteUri =
        // Uri stockQuoteUri =
        Cursor data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                null,
                null,
                null,
                null);

        if( data == null) {
            //The case of no data in cursor so display empty view TODO
            Log.v("Widget", "Data in Cursor is Empty");
            return;
        }

        if(!data.moveToFirst()) {
            data.close();
            return;
        }

        //Extract stock Data from the Cursor
        /* TODO
        int weatherId = data.getInt(INDEX_WEATHER_ID);
        int weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
        String description = data.getString(INDEX_SHORT_DESC);
        double maxTemp = data.getDouble(INDEX_MAX_TEMP);
        String formattedMaxTemperature = Utility.formatTemperature(this, maxTemp);
        data.close();
        */
        // Perform this loop procedure for each Stock widget

        String stockSymbol = data.getString(INDEX_SYMBOL);
        String yData = DatabaseUtils.dumpCursorToString(data);
        Log.v("yyCursor", "DBQDump = " + DatabaseUtils.dumpCursorToString(data));
        Log.v("yystockSymbol", stockSymbol);
        data.close();

        for (int appWidgetId : appWidgetIds) {
           // int layoutId = R.layout.widget_stock_quote_small;
            int layoutId = R.layout.widget_detail;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            views.setTextViewText(R.id.stock_symbol, stockSymbol);

            //Add data to RemoteViews
            /* TODO if using images
            views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);

            // Content Descriptions for RemoteViews were only added in ICS MR1
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }*/

        //yyo     views.setTextViewText(R.id.stock_value, stockSymbol);

            //Create Intent to Launch MyStocksActivity
            Intent launchIntent = new Intent(this, MyStocksActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);


        }


    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        // yy  views.setContentDescription(R.id.widget_icon, description);
    }
}
