package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by yusuf on 17/06/16.
 */
public class StockDetailTaskService extends GcmTaskService {
    private String LOG_TAG = StockDetailTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;

    public static final String ACTION_DATA_UPDATED =
            "com.sam_chordas.android.stockhawk.ACTION_DATA_UPDATED";

    public StockDetailTaskService(){}

    public StockDetailTaskService(Context context){
        mContext = context;
    }
    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    @Override
    public int onRunTask(TaskParams params){
        Cursor initQueryCursor;
        if (mContext == null){
            mContext = this;
        }
        StringBuilder urlStringBuilder = new StringBuilder();
        try{
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol "
                    + "= \"", "UTF-8"));

          //  urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
           //         + "in (", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // else if (params.getTag().equals("add")){
        isUpdate = false;
        // get symbol from params.getExtra and build query
        String stockInput = params.getExtras().getString("stock_symbol");
        Log.v("stockinput", stockInput);
        try {
            urlStringBuilder.append(URLEncoder.encode(stockInput + "\"" + " and startDate = ", "UTF-8"));
           // urlStringBuilder.append(URLEncoder.encode("\""+stockInput+"\")", "UTF-8"));
            String startDate="2009-09-11";
            String endDate="2010-03-10";
            urlStringBuilder.append(URLEncoder.encode("\""+startDate + "\"" + " and endDate = ", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("\""+endDate + "\"", "UTF-8"));
            Log.v("str1", urlStringBuilder.toString());
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }

        // finalize the URL for the API query.
       // urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
         //       + "org%2Falltableswithkeys&callback=");

        String urlString;
        String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;

        if (urlStringBuilder != null){
            urlString = urlStringBuilder.toString();
            Log.v("StringBuilder", urlString);
            try{
                getResponse = fetchData(urlString);
                Log.v("fetchdata", getResponse);
                result = GcmNetworkManager.RESULT_SUCCESS;
                //try {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(mContext.getString(R.string.share_pref_close_value_json),getResponse);

              //  Utils.historyQuoteJsonToCloseValueList(mContext, getResponse);
               // Utils.historyQuoteJsonToDateList(mContext,getResponse);
                    ContentValues contentValues = new ContentValues();
                    Log.v("contentvalues", contentValues.toString());
                    // update ISCURRENT to 0 (false) so new data is current
                    if (isUpdate){
                        Log.v("contentvalues1", contentValues.toString());
                       // contentValues.put(QuoteColumns.ISCURRENT, 0);
                      //yy  mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                       //yy         null, null);
                    }
                  //yy  mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                   //yy         Utils.quoteJsonToContentVals(mContext, getResponse));
             //   }catch (RemoteException | OperationApplicationException e){
               /*ycacth }catch (RemoteException | OperationApplicationException e){
                    Log.e(LOG_TAG, "Error applying batch insert", e);
                }*/
            } catch (IOException e){
                e.printStackTrace();
            }

        }
Log.v("GCMresult", "Result is"+result);
        return result;
    }



}

