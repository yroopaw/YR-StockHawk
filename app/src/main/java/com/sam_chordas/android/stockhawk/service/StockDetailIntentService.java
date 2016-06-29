package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;

/**
 * Created by yusuf on 17/06/16.
 */
public class StockDetailIntentService extends IntentService {


    public StockDetailIntentService() {
        super(StockDetailIntentService.class.getName());
    }

    public StockDetailIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(StockDetailIntentService.class.getSimpleName(), "Stock Detail Intent Service");
        StockDetailTaskService stockDetailTaskService = new StockDetailTaskService(this);
        Bundle args = new Bundle();
        args.putString("stock_symbol", intent.getStringExtra("stock_symbol"));

        String stockSymbol = intent.getStringExtra("stock_symbol");
        Log.v("SDIS", stockSymbol);

        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        // stockDetailTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
        stockDetailTaskService.onRunTask(new TaskParams("stock_symbol", args));
    }


}
