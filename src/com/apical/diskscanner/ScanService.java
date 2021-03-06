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
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ScanService extends Service
{
    private static final String TAG = "ScanService";
    private ScanBinder    mBinder   = null;
    private Handler       mHandler  = null;
    private List<String>  mFileList = null;
    private List<DirItem> mDirList  = null;
    private Thread        mThread   = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mBinder   = new ScanBinder();
        mFileList = new ArrayList ();
        mDirList  = new ArrayList ();

        // register receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED  );
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT    );
        filter.addDataScheme("file");
        registerReceiver(mMediaChangeReceiver, filter);

        // start scan thread
        setScanDiskThreadStart(true , "/mnt/extsd");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        // stop scan thread
        setScanDiskThreadStart(false, null);

        // unregister receiver
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

    public List<String> getFileList() {
        return mFileList;
    }

    public List<DirItem> getDirList() {
        return mDirList;
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
            if (subfiles != null) {
                for (File f : subfiles) {
                    if (mStopScan) break;
                    if (f.isDirectory()) {
                        doScanDisk(f.getAbsolutePath());
                    } else {
                        mFileList.add(f.getAbsolutePath());
//                      Log.i(TAG, "add " + f.getAbsolutePath());
                        if (SystemClock.uptimeMillis() - mLastTime > 500) {
                            mLastTime = SystemClock.uptimeMillis();
                            sendMessage(MainActivity.MSG_UDPATE_LISTVIEW, 0, 0, 0);
                        }
                    }
                }
            }
        }
    }

    private void doScanDiskBFS(String dir) {
        Queue<String> dirlist = new LinkedList<String>();
        dirlist.offer(dir);

        int     offset = 0;
        int     number = 0;
        DirItem diritem= null;
        while (!dirlist.isEmpty()) {
            String curdir = dirlist.poll();
            File fdir = new File(curdir);
            if (fdir.exists()) {
                File[] subfiles = fdir.listFiles();
                if (subfiles != null) {
                    number  = 0;
                    diritem = null;
                    for (File f : subfiles) {
                        if (mStopScan) break;
                        if (f.isDirectory()) {
                            dirlist.offer(f.getAbsolutePath());
                        } else {
                            if (f.getAbsolutePath().endsWith(".mp4")) {
                                if (number == 0) {
                                    diritem = new DirItem(curdir, offset, number);
                                    mDirList.add(diritem);
                                }
                                mFileList.add(f.getAbsolutePath());
                                offset++; number++;
                                if (diritem != null) diritem.number = number;
                            }
                            if (SystemClock.uptimeMillis() - mLastTime > 500) {
                                mLastTime = SystemClock.uptimeMillis();
                                sendMessage(MainActivity.MSG_UDPATE_LISTVIEW, 0, 0, 0);
                            }
//                          try { Thread.sleep(100); } catch (Exception e) { e.printStackTrace(); }
                        }
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
            mThread   = new Thread() {
                @Override
                public void run() {
//                  doScanDisk(path);
                    doScanDiskBFS(path);
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
                    mFileList.clear();
                    mDirList .clear();
                    sendMessage(MainActivity.MSG_UDPATE_LISTVIEW, 0, 0, 0);
                }
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                Log.i(TAG, "Intent.ACTION_MEDIA_MOUNTED = " + path);
                if (path.contains("extsd")) {
                    mFileList.clear();
                    mDirList .clear();
                    setScanDiskThreadStart(true , path);
                }
            }
        }
    };
}


