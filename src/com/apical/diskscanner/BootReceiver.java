package com.apical.diskscanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private static final boolean ENABLE_DISKSCAN_SERVICE = true;

    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            if (ENABLE_DISKSCAN_SERVICE) {
                Intent service = new Intent(context, com.apical.diskscanner.ScanService.class);
                context.startService(service);
            }
        } catch (Exception e) {
            Log.e(TAG, "Can't start load record service", e);
        }
    }
}

