package com.jgh.androidssh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jcraft.jsch.ChannelSftp;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Adapter for holding data of remote files
 * in remote host's current directory.
 * Created by jon on 4/12/14.
 */
public class RemoteFileListAdapter extends BaseAdapter {

    private Vector<ChannelSftp.LsEntry> mFiles;
    private Context mContext;
    private LayoutInflater mInflater;

    //
    // Constructor
    //
    public RemoteFileListAdapter(Context context, Vector<ChannelSftp.LsEntry> files) {
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

        holder.textView.setText(mFiles.get(position).getFilename());
        if (mFiles.get(position).getAttrs().isDir()) {
            holder.color = 0xff0099ff;
        }
        else{
            holder.color = 0xffff8888;
        }

        holder.textView.setTextColor(holder.color);
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


    public Vector<ChannelSftp.LsEntry> getRemoteFiles(){
        return mFiles;
    }


}
