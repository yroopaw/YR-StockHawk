package com.sam_chordas.android.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Toast;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MyStockDetailsActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    //added by Yusuf
    private Boolean isConnected;
    private Context mContext;
    private String mStockSymbol;
    private static ArrayList<Entry> mEntries = new ArrayList<>();
    private static ArrayList<String> mLabels = new ArrayList<>();
    private LineChart mLineChart;
    private static String LOG_TAG = MyStockDetailsActivity.class.getSimpleName();
    //added by Yusuf
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // yychart setContentView(R.layout.activity_my_stock_details);
        setContentView(R.layout.activity_line_graph);
        ActionBar actionBar = getActionBar();
        Intent stockDetailIntent = getIntent();
        mStockSymbol = stockDetailIntent.getStringExtra("stock_symbol");
        String stockSymbol = mStockSymbol;
        Log.v("SSTK", stockSymbol);


        //Added to call get Stock Details for plotting 19-6-2016
        mContext = this;
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
      //  setContentView(R.layout.activity_my_stocks);
        // The intent service is for executing immediate pulls from the Yahoo API for Detail Stock
     //yyq   Intent stockDetailServiceIntent = new Intent(this, StockDetailIntentService.class);

        if (savedInstanceState == null) {
            // Run the initialize task service so that some stocks appear upon an empty database
         //yyq   stockDetailServiceIntent.putExtra("stock_symbol", stockSymbol);
            if (isConnected) {
                FetchHistoricalQuotesTask fetchHistoricalQuotesTask = new FetchHistoricalQuotesTask();
                fetchHistoricalQuotesTask.execute();
            //    startService(stockDetailServiceIntent);
            } else {
                networkToast();
            }
        }


        //SharedPreference to get data from StockDetail Service
     //   SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
     //   String jsonStockHistory = sharedPrefs.getString(mContext.getString(R.string.share_pref_close_value_json), null);

     //   Log.v("fromStockDetailTask",jsonStockHistory);

        //Added to call get Stock Details for plotting 19-6-2016

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
/*For linechart
        TextView tv1 = (TextView)mContentView;
        Log.v("YSSTK", stockSymbol);
        tv1.setText(stockSymbol);

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        */

       // BarChart chart = (BarChart) findViewById(R.id.chart);
      //yc  LineChart chart = (LineChart) findViewById(R.id.chart);
         mLineChart = (LineChart) findViewById(R.id.chart);
//        BarData data = new BarData(getXAxisValues(), getDataSet());

       // LineData data = new LineData(getXAxisValues(), getDataSet());
     //yc   LineDataSet dataset = new LineDataSet(mEntries, "Historical Quote");
       //yc LineData data = new LineData(mLabels, dataset);

       //yc chart.setData(data);
       //yc chart.setDescription("My Chart");
       // chart.setColors(ColorTemplate.VORDIPLOM_COLORS);
      /*yc  chart.getAxisLeft().setDrawLabels(true);
        chart.getAxisRight().setDrawLabels(true);
        chart.getXAxis().setDrawLabels(true);
        chart.getXAxis().setAvoidFirstLastClipping(true);
        chart.getLegend().setEnabled(true);
        chart.animateXY(2000,2000);
        chart.invalidate();*/
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggle() {
        /*if (mVisible) {
            hide();
        } else {
            show();
        }*/
    }

    private void hide() {
        // Hide UI first
      /*  ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);*/
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void networkToast() {
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }









    public class FetchHistoricalQuotesTask extends AsyncTask<String, Void, String> {




        @Override
        protected String doInBackground(String... params) {

            // These two need to be declared outside the try / catch
            // so that they can be closed in the finally block.

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String historicalQuoteJsonStr = null;
            String monthSeperator = "-";
            String dateSeperator = "-";

            try {

                // Construct the URL for the yahoo query
                StringBuilder urlStringBuilder = new StringBuilder();

                // Base URL for the Yahoo query
                urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol "
                            + "= \"", "UTF-8"));
                urlStringBuilder.append(URLEncoder.encode(mStockSymbol + "\"" + " and startDate = ", "UTF-8"));

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH)+1;
                int date = calendar.get(Calendar.DATE);
                Log.v(LOG_TAG, "Year:"+year+":Month:"+month+":date:"+date);

                if (month <10) {
                     monthSeperator += "0";
                }


                if (date <10) {
                     dateSeperator += "0";
                }

                String startDate= (year-1)+monthSeperator+month+dateSeperator+date;
                String endDate= year+monthSeperator+month+dateSeperator+date;
                //String endDate="2010-03-10";

                urlStringBuilder.append(URLEncoder.encode("\""+startDate + "\"" + " and endDate = ", "UTF-8"));
                urlStringBuilder.append(URLEncoder.encode("\""+endDate + "\"", "UTF-8"));

                urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                       + "org%2Falltableswithkeys&callback=");

                URL url = new URL(urlStringBuilder.toString());


                Log.v(LOG_TAG, "Built URI " + urlStringBuilder.toString());

                // Create the request to yahoo, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {

                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                historicalQuoteJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Trailer JSON String: " + historicalQuoteJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }


            return historicalQuoteJsonStr;



        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected void onPostExecute(String result) {

            if (result != null) {
                historyQuoteJsonToLineChartList(result);

            }


            for (String s : mLabels) {
                Log.v(LOG_TAG, "Labels : " + s);
            }

            for (Entry e : mEntries) {
                Log.v(LOG_TAG, "Entry : " + e);
            }


            LineDataSet dataset = new LineDataSet(mEntries, "Historical Quote");
            dataset.setDrawCircles(true);
            dataset.setDrawValues(true);
            mLineChart.setBackgroundColor(Color.WHITE);
            dataset.setColor(Color.GREEN);
            LineData data = new LineData(mLabels, dataset);
            mLineChart.setDescription("Historical Quotes: " + mStockSymbol);
            mLineChart.setData(data);
            mLineChart.getAxisRight().setDrawLabels(true);
            mLineChart.getXAxis().setDrawLabels(true);



            mLineChart.animateY(2000);

        }

    }

    public void historyQuoteJsonToLineChartList(String JSON){

       //ArrayList<Entry> entries = new ArrayList<>();
      //  ArrayList<String> labels = new ArrayList<>();

        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        mEntries.clear();
        mLabels.clear();

        try{
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0){
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));

                resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");
                Log.v(LOG_TAG, "ResultsArray Length " + resultsArray.length());

                if (resultsArray != null && resultsArray.length() != 0){
                   // for (int i = 0; i < resultsArray.length(); i++){
                    for (int  i = resultsArray.length()-1; i > 0; i--){
                        jsonObject = resultsArray.getJSONObject(i);
                        String closeValue = jsonObject.getString("Close");
                        String closeValueDate = jsonObject.getString("Date");


                        mEntries.add(new Entry(Float.parseFloat(closeValue),i));
                        mLabels.add(closeValueDate);

                    }
                }

            }
        } catch (JSONException e){
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }

    }


}
