package com.jgh.androidssh.adapters;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Adapter for ListView
 *
 * @author Jonathan Hough
 */
public class FileListAdapter extends BaseAdapter {

    private ArrayList<File> mFiles;
    private Context mContext;
    private LayoutInflater mInflater;

    //
    // Constructor
    //
    public FileListAdapter(Context context, ArrayList<File> files) {
        mContext = context;
        mFiles = files;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return mFiles.size();
    }

    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(com.jgh.androidssh.R.layout.listview_item, null);

            TextView textView = (TextView) convertView.findViewById(com.jgh.androidssh.R.id.textview_item);
            holder.textView = textView;
            // change text color for directories
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(mFiles.get(position).getName());
        if (mFiles.get(position).isDirectory()) {
            holder.color = 0xff009999;
        }
        else{
            holder.color = 0xff558899;
        }

        holder.textView.setTextColor(holder.color);

        //setName(position, holder.textView);
        return convertView;
    }



    /**
     * Private view holder class
     *
     * @author Jonathan Hough
     */
    private class ViewHolder {

        TextView textView;
        int color;
    }




}
