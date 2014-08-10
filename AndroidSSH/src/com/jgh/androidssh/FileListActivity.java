package com.jgh.androidssh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jgh.androidssh.adapters.FileListAdapter;
import com.jgh.androidssh.adapters.RemoteFileListAdapter;
import com.jgh.androidssh.sshutils.SessionUserInfo;
import com.jgh.androidssh.sshutils.SftpExec;
import com.jgh.androidssh.sshutils.SshExecutor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.view.DragEvent;

import com.jgh.androidssh.sshutils.SessionController;
import com.jgh.androidssh.sshutils.TaskCallbackHandler;

import android.util.Log;
import android.widget.TextView;

/**
 * Activity to list files. Uploads chosen files to server (SFTP) using an
 * AsyncTask.
 *
 * @author Jonathan Hough
 * @since 7 Dec 2012
 */
public class FileListActivity extends Activity implements OnItemClickListener, OnClickListener, OnDragListener {

    private static final String TAG = "FileListActivity";
    private ArrayList<File> mFilenames = new ArrayList<File>();
    private ListView mListView, mRemoteListView;
    private FileListAdapter mFileListAdapter;
    private RemoteFileListAdapter mRemoteFileListAdapter;
    private String[] mUserInfo;
    private File mRootFile;
    private Button mUpButton, mConnectButton;
    private TextView mStateView;
    private SessionController mSessionController;
    private RemoteClickListener mRemoteClickListener;
    private Vector<ChannelSftp.LsEntry> mRemoteFiles;
    private boolean mIsProcessing = false;

    @Override
    public boolean onDrag(View v, DragEvent event) {
        return true;
    }


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_filelistactivity);

        mUserInfo = getIntent().getExtras().getStringArray("UserInfo");
        mListView = (ListView) findViewById(R.id.listview);
        mRemoteListView = (ListView) findViewById(R.id.remotelistview);
        // Get external storage
        mRootFile = Environment.getExternalStorageDirectory();
        // list files
        for (File f : mRootFile.listFiles()) {
            mFilenames.add(f);
        }

        mFileListAdapter = new FileListAdapter(this, mFilenames);

        mListView.setAdapter(mFileListAdapter);
        mListView.setOnItemClickListener(this);
        //----------------- buttons ---------------//
        mUpButton = (Button) findViewById(R.id.upbutton);
        mUpButton.setOnClickListener(this);
        mConnectButton = (Button) findViewById(R.id.connectbutton);
        mConnectButton.setOnClickListener(this);

        mStateView = (TextView) findViewById(R.id.statetextview);


        SessionUserInfo sui = new SessionUserInfo(mUserInfo[0], mUserInfo[1], mUserInfo[2]);
        mSessionController = SessionController.getSessionController();
        mSessionController.connect();

        mRemoteClickListener = new RemoteClickListener();
        mRemoteListView.setOnItemClickListener(mRemoteClickListener);


        if (mSessionController.getSession().isConnected()) {
            mStateView.setText("Connected");
            showRemoteFiles();

        } else {
            mStateView.setText("Disconnected");
        }

    }

    /**
     * @param remoteFileListAdapter
     */
    public void setupRemoteFiles(RemoteFileListAdapter remoteFileListAdapter) {
        mRemoteFileListAdapter = remoteFileListAdapter;
        mRemoteListView.setAdapter(mRemoteFileListAdapter);
        remoteFileListAdapter.notifyDataSetChanged();

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
            Log.v(TAG, "ROOT FILE POSIITON IS " + mRootFile);
            mFilenames.clear();
            if (mRootFile.listFiles() == null) {
                return;
            }
            for (File f : mRootFile.listFiles()) {
                mFilenames.add(f);
            }

            setAdapter(mFilenames);
            mListView.setAdapter(mFileListAdapter);
            mFileListAdapter.notifyDataSetChanged();

        } else {
            // sftp the file
            SftpProgressDialog progressDialog = new SftpProgressDialog(this, 0);
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            File[] arr = {mFilenames.get(position)};
            mSessionController.uploadFiles(arr, progressDialog);
        }

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


    /**
     * Shows the remote files on the listview.
     */
    private void showRemoteFiles() {
        final ProgressDialog progressDialog = new ProgressDialog(FileListActivity.this, 0);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(R.string.retrieve_remote_files);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        try {
            mSessionController.listRemoteFiles(new TaskCallbackHandler() {
                @Override
                public void OnBegin() {
                    progressDialog.show();
                }

                @Override
                public void onFail() {
                    Log.e(TAG, "Fail listing remote files");
                    progressDialog.dismiss();
                }

                @Override
                public void onTaskFinished(Vector<ChannelSftp.LsEntry> lsEntries) {
                    mRemoteFileListAdapter = new RemoteFileListAdapter(FileListActivity.this, lsEntries);
                    mRemoteListView.setAdapter(mRemoteFileListAdapter);
                    mRemoteFileListAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                }
            }, "");
        } catch (JSchException j) {
            Log.e(TAG, "ShowRemoteFiles exception " + j.getMessage());
            progressDialog.dismiss();
        } catch (SftpException s) {
            Log.e(TAG, "ShowRemoteFiles exception " + s.getMessage());
            progressDialog.dismiss();
        }
    }


    private class SftpProgressDialog extends ProgressDialog implements SftpProgressMonitor {

        /**
         * Size of file to transfer
         */
        private long mSize = 0;
        /**
         * Current progress count
         */
        private long mCount = 0;

        /**
         * Constructor
         *
         * @param context
         * @param theme
         */

        public SftpProgressDialog(Context context, int theme) {
            super(context, theme);
            // TODO Auto-generated constructor stub
        }

        //
        // SftpProgressMonitor methods
        //

        /**
         * Gets the data uploaded since the last count.
         */
        public boolean count(long arg0) {
            mCount += arg0;
            this.setProgress((int) ((float) (mCount) / (float) (mSize) * (float) getMax()));
            return true;
        }

        /**
         * Data upload is ended. Dismiss progress dialog.
         */
        public void end() {
            this.setProgress(this.getMax());
            this.dismiss();

        }

        /**
         * Initializes the SftpProgressMonitor
         */
        public void init(int arg0, String arg1, String arg2, long arg3) {
            mSize = arg3;

        }


    }


    /**
     * Listener class for remote file list click events. Handles file and directory clicks
     * from user.
     */
    private class RemoteClickListener implements OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            if (mIsProcessing) {
                return;
            }
            if (mRemoteFileListAdapter == null) {
                return;
            }
            //Is directory?
            if (mRemoteFileListAdapter.getRemoteFiles().get(position).getAttrs().isDir()
                    || mRemoteFileListAdapter.getRemoteFiles().get(position).getFilename().trim() == "..") {

                final ProgressDialog progressDialog = new ProgressDialog(FileListActivity.this, 0);
                progressDialog.setIndeterminate(true);
                progressDialog.setTitle(R.string.retrieve_remote_files);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                try {
                    mIsProcessing = true;
                    mSessionController.listRemoteFiles(new TaskCallbackHandler() {


                        @Override
                        public void OnBegin() {
                            progressDialog.show();
                        }

                        @Override
                        public void onFail() {
                            mIsProcessing = false;
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onTaskFinished(Vector<ChannelSftp.LsEntry> lsEntries) {
                            mRemoteFileListAdapter = new RemoteFileListAdapter(FileListActivity.this, lsEntries);
                            mRemoteListView.setAdapter(mRemoteFileListAdapter);
                            mRemoteFileListAdapter.notifyDataSetChanged();
                            mIsProcessing = false;
                            progressDialog.dismiss();

                        }
                    }, mRemoteFileListAdapter.getRemoteFiles().get(position).getFilename());
                } catch (JSchException j) {
                    Log.e(TAG, "Error on remote file click " + j.getMessage());
                    progressDialog.dismiss();
                } catch (SftpException s) {
                    Log.e(TAG, "Error on remote file click " + s.getMessage());
                    progressDialog.dismiss();
                }

            } else {

                // sftp the file
                SftpProgressDialog progressDialog = new SftpProgressDialog(FileListActivity.this, 0);
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();

                try {
                    String name = mRemoteFileListAdapter.getRemoteFiles().get(position).getFilename();
                    String out = mRootFile.getAbsolutePath() + "/" + name;

                    mSessionController.downloadFile(mRemoteFileListAdapter.getRemoteFiles().get(position).getFilename(), out, progressDialog);
                } catch (JSchException je) {
                    Log.v(TAG, "JschException " + je.getMessage());
                } catch (SftpException se) {
                    Log.v(TAG, "SftpException " + se.getMessage());
                }
            }


        }
    }


}
