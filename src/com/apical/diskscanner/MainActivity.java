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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.util.Log;

import java.util.List;

public class MainActivity extends Activity
{
    private static final String TAG = "MainActivity";
    private ListView        mListViewDir;
    private ListView        mListViewFile;
    private ListAdapterDir  mListAdapterDir;
    private ListAdapterFile mListAdapterFile;
    private int             mSelectedDirItem;

    private ScanService mScanServ = null;
    private ServiceConnection mScanServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder serv) {
            mScanServ        = ((ScanService.ScanBinder)serv).getService(mHandler);
            mListAdapterDir  = new ListAdapterDir (MainActivity.this, mScanServ.getDirList ());
            mListAdapterFile = new ListAdapterFile(MainActivity.this, mScanServ.getFileList());
            mListViewDir .setAdapter(mListAdapterDir );
            mListViewFile.setAdapter(mListAdapterFile);
            mListAdapterDir .notifyDataSetChanged();
            mListAdapterFile.notifyDataSetChanged();
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

        mListViewDir  = (ListView)findViewById(R.id.lv_dir_list );
        mListViewFile = (ListView)findViewById(R.id.lv_file_list);
        mListViewDir .setOnItemClickListener(mDirListItemClickListener);
        mListViewDir .setVisibility(View.VISIBLE);
        mListViewFile.setVisibility(View.VISIBLE);

        mSelectedDirItem = -1;

        // start record service
        Intent i = new Intent(MainActivity.this, ScanService.class);
//      startService(i);

        // bind record service
        bindService(i, mScanServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        // unbind record service
        unbindService(mScanServiceConn);

        // stop record service
        /*
        Intent i = new Intent(MainActivity.this, ScanService.class);
        stopService(i);
        */

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

    private AdapterView.OnItemClickListener mDirListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mSelectedDirItem = position;
            mListAdapterDir.setSelected(position);
            mHandler.sendEmptyMessage(MSG_UDPATE_LISTVIEW);
        }
    };

    public static final int MSG_UDPATE_LISTVIEW = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UDPATE_LISTVIEW:
                List<DirItem> list = mScanServ.getDirList();
                if (list.isEmpty()) {
                    mSelectedDirItem = -1;
                    mListAdapterDir.setSelected(-1);
                    mListAdapterFile.setOffNum(0, 0);
                } else if (mSelectedDirItem >= 0 && mSelectedDirItem < list.size()) {
                    int offset = list.get(mSelectedDirItem).offset;
                    int number = list.get(mSelectedDirItem).number;
                    mListAdapterFile.setOffNum(offset, number);
                }
                mListAdapterDir .notifyDataSetChanged();
                mListAdapterFile.notifyDataSetChanged();
                break;
            }
        }
    };
}




