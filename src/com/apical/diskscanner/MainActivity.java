package com.apical.diskscanner;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.util.Log;

public class MainActivity extends Activity
{
    private static final String TAG = "MainActivity";
    private ListView        mListViewFile;
    private FileListAdapter mListAdapter;

    private ScanService mScanServ = null;
    private ServiceConnection mScanServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder serv) {
            mScanServ    = ((ScanService.ScanBinder)serv).getService(mHandler);
            mListAdapter = mScanServ.getFileListAdapter();
            mListViewFile.setAdapter(mListAdapter);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mScanServ = null;
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mListViewFile = (ListView)findViewById(R.id.lv_file_list );

        // start record service
        Intent i = new Intent(MainActivity.this, ScanService.class);
        startService(i);

        // bind record service
        bindService(i, mScanServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        // unbind record service
        unbindService(mScanServiceConn);

        // stop record service
        Intent i = new Intent(MainActivity.this, ScanService.class);
        stopService(i);

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public static final int MSG_UDPATE_LISTVIEW = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UDPATE_LISTVIEW:
                mListAdapter.update();
                mListAdapter.notifyDataSetChanged();
                break;
            }
        }
    };
}




