package com.apical.diskscanner;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.ArrayList;

class FileListAdapter extends BaseAdapter {
    private Context      mContext  = null;
    private List<String> mFileList = null;
    private int          mCount    = 0;

    public FileListAdapter(Context context, List<String> list) {
        mContext  = context;
        mFileList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.file_item, null);
            holder = new ViewHolder();
            holder.fi_name = (TextView) convertView.findViewById(R.id.fi_file_name);
            holder.fi_name.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.fi_name.setText(mFileList.get(position));
        return convertView;
    }

    @Override
    public final int getCount() {
//      return mFileList.size();
        return mCount;
    }

    @Override
    public final Object getItem(int position) {
        return mFileList.get(position);
    }

    @Override
    public final long getItemId(int position) {
        return position;
    }

    @Override
    public void notifyDataSetChanged() {
        mCount = mFileList.size();
        super.notifyDataSetChanged();
    }

    class ViewHolder {
        TextView fi_name;
    }
}
