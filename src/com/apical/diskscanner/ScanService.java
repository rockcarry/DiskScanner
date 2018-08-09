package com.apical.diskscanner;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.widget.BaseAdapter;
import android.util.Log;

import java.io.*;

public class ScanService extends Service
{
    private static final String TAG = "ScanService";
    private ScanBinder      mBinder  = null;
    private Handler         mHandler = null;
    private FileListAdapter mAdapter = null;
    private Thread          mThread  = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mBinder  = new ScanBinder();
        mAdapter = new FileListAdapter(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT  );
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mMediaChangeReceiver, filter);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        unregisterReceiver(mMediaChangeReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public class ScanBinder extends Binder {
        public ScanService getService(Handler h) {
            mHandler = h;
            return ScanService.this;
        }
    }

    public FileListAdapter getFileListAdapter() {
        return mAdapter;
    }

    private void sendMessage(int what, int arg1, int arg2, Object obj) {
        if (mHandler == null) return;
        Message msg = new Message();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj  = obj;
        mHandler.sendMessage(msg);
    }

    private boolean mStopScan = false;
    private long    mLastTime = 0;
    private void doScanDisk(String dir) {
        File fdir = new File(dir);
        if (fdir.exists()) {
            File[] subfiles = fdir.listFiles();
            for (File f : subfiles) {
                if (mStopScan) break;
                if (f.isDirectory()) {
                    doScanDisk(f.getAbsolutePath());
                } else {
                    mAdapter.add(f.getAbsolutePath());
                    if (SystemClock.uptimeMillis() - mLastTime > 500) {
                        mLastTime = SystemClock.uptimeMillis();
                        sendMessage(MainActivity.MSG_UDPATE_LISTVIEW, 0, 0, 0);
                    }
                }
            }
        }
    }

    private void setScanDiskThreadStart(boolean start, final String path) {
        if (start) {
            if (mThread != null) return;
            mStopScan = false;
            mLastTime = SystemClock.uptimeMillis();
            mThread = new Thread() {
                @Override
                public void run() {
                    doScanDisk(path);
                    sendMessage(MainActivity.MSG_UDPATE_LISTVIEW, 0, 0, 0);
                    mThread = null;
                }
            };
            mThread.start();
        } else {
            mStopScan = true;
            if (mThread != null) {
                try { mThread.join(); } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    private BroadcastReceiver mMediaChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final Uri uri = intent.getData();
            String   path = uri.getPath();

            if (  action.equals(Intent.ACTION_MEDIA_EJECT)
               || action.equals(Intent.ACTION_MEDIA_UNMOUNTED) ) {
                Log.i(TAG, "Intent.ACTION_MEDIA_EJECT path = " + path);
                if (path.contains("extsd")) {
                    setScanDiskThreadStart(false, path);
                    mAdapter.empty();
                    sendMessage(MainActivity.MSG_UDPATE_LISTVIEW, 0, 0, 0);
                }
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                Log.i(TAG, "Intent.ACTION_MEDIA_MOUNTED = " + path);
                if (path.contains("extsd")) {
                    setScanDiskThreadStart(true , path);
                }
            }
        }
    };
}


