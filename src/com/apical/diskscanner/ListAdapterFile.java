package com.apical.diskscanner;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

class ListAdapterFile extends BaseAdapter {
    private Context      mContext  = null;
    private List<String> mFileList = null;
    private int          mOffset   = 0;
    private int          mCount    = 0;

    public ListAdapterFile(Context context, List<String> list) {
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

        String filepath = mFileList.get(mOffset + position);
        String filename = (new File(filepath)).getName();
        holder.fi_name.setText(filename);
        return convertView;
    }

    @Override
    public final int getCount() {
//      return mFileList.size();
        return mCount;
    }

    @Override
    public final Object getItem(int position) {
        return mFileList.get(mOffset + position);
    }

    @Override
    public final long getItemId(int position) {
        return mOffset + position;
    }

    @Override
    public void notifyDataSetChanged() {
//      mCount = mFileList.size();
        super.notifyDataSetChanged();
    }

    public void setOffNum(int off, int num) {
        mOffset = off;
        mCount  = num;
    }

    class ViewHolder {
        TextView fi_name;
    }
}
