package com.apical.diskscanner;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

class ListAdapterDir extends BaseAdapter {
    private Context       mContext = null;
    private List<DirItem> mDirList = null;
    private int           mCount   = 0;
    private int           mSelected= -1;

    public ListAdapterDir(Context context, List<DirItem> list) {
        mContext = context;
        mDirList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.dir_item, null);
            holder = new ViewHolder();
            holder.di_bg   = (LinearLayout) convertView.findViewById(R.id.di_dir_bg);
            holder.di_name = (TextView) convertView.findViewById(R.id.di_dir_name);
            holder.di_name.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DirItem item = mDirList.get(position);
        if (mSelected == position) {
            holder.di_bg.setBackgroundResource(R.drawable.item_sel);
        } else {
            holder.di_bg.setBackground(convertView.getBackground());
        }

        int    offset  = item.offset;
        int    number  = item.number;
        String dirpath = item.dir;
        File   file    = new File(dirpath);
        String dirname = file.getName();
        holder.di_name.setText(String.format("%s (%d/%d)\n%s", dirname, offset, number, dirpath));
        return convertView;
    }

    @Override
    public final int getCount() {
//      return mDirList.size();
        return mCount;
    }

    @Override
    public final Object getItem(int position) {
        return mDirList.get(position);
    }

    @Override
    public final long getItemId(int position) {
        return position;
    }

    @Override
    public void notifyDataSetChanged() {
        mCount = mDirList.size();
        super.notifyDataSetChanged();
    }

    public void setSelected(int sel) {
        mSelected = sel;
    }

    class ViewHolder {
        TextView     di_name;
        LinearLayout di_bg;
    }
}
