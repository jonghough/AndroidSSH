package com.jgh.androidssh;

import java.io.File;
import java.util.ArrayList;

import com.jgh.androidssh.adapters.FileListAdapter;
import com.jgh.androidssh.services.SftpService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

/**
 * Activity to list files.
 * 
 * @author Jonathan Hough
 */
public class FileListActivity extends Activity implements OnItemClickListener, OnClickListener {
    
    private ArrayList<File> mFilenames = new ArrayList<File>();
    
    private ListView mListView;
    
    private FileListAdapter mFileListAdapter;
    
    private String[] mUserInfo;
    
    private File mRootFile;
    
    private Button mUpButton;
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        
        setContentView(R.layout.activity_filelistactivity);
        
        mUserInfo = getIntent().getExtras().getStringArray("UserInfo");
        
        mListView = (ListView)findViewById(R.id.listview);
        
        // Get external storage
        mRootFile = Environment.getExternalStorageDirectory();
        // list files
        for (File f : mRootFile.listFiles()) {
            mFilenames.add(f);
        }
        
        mFileListAdapter = new FileListAdapter(this, mFilenames);
        
        mListView.setAdapter(mFileListAdapter);
        mListView.setOnItemClickListener(this);
        
        mUpButton = (Button)findViewById(R.id.upbutton);
        mUpButton.setOnClickListener(this);
        
    }
    
    /**
     * Sets the adapter with the given array list
     * 
     * @param files
     */
    private void setAdapter(ArrayList<File> files) {
        mFileListAdapter = new FileListAdapter(this, files);
    }
    
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        // change the list
        if (mFilenames.get(position).isDirectory()) {
            mRootFile = mFilenames.get(position);
            mFilenames.clear();
            if (mRootFile.listFiles() == null) { return; }
            for (File f : mRootFile.listFiles()) {
                mFilenames.add(f);
            }
            
            setAdapter(mFilenames);
            mListView.setAdapter(mFileListAdapter);
            mFileListAdapter.notifyDataSetChanged();
            
        } else {
            // sftp the file
            startIntentService(mFilenames.get(position));
        }
        
    }
    
    /**
     * Starts a service to perform the file transfer.
     */
    private void startIntentService(File file) {
        
        Intent intent = new Intent(this, SftpService.class);
        
        String[] files = {file.getPath()};
        
        intent.putExtra("Files", files);
        intent.putExtra("UserInfo", mUserInfo);
        
        startService(intent);
    }
    
    // go back up to parent folder
    public void onClick(View v) {
        if (v == mUpButton) {
            
            boolean hasParent = mRootFile.getParentFile() == null ? false : true;
            if (hasParent) {
                mRootFile = mRootFile.getParentFile();
                
                mFilenames.clear();
                for (File f : mRootFile.listFiles()) {
                    mFilenames.add(f);
                }
                
                setAdapter(mFilenames);
                mListView.setAdapter(mFileListAdapter);
                mFileListAdapter.notifyDataSetChanged();
            }
            
        }
        
    }
    
}
