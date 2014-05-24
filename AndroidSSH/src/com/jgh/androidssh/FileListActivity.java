package com.jgh.androidssh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
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

        mStateView = (TextView)findViewById(R.id.statetextview);


        SessionUserInfo sui = new SessionUserInfo(mUserInfo[0], mUserInfo[1], mUserInfo[2]);
        mSessionController = SessionController.getSessionController();
        mSessionController.connect();

        mRemoteClickListener = new RemoteClickListener();
        mRemoteListView.setOnItemClickListener(mRemoteClickListener);

        if(mSessionController.getSession().isConnected()){
            mStateView.setText("Connected");
            showRemoteFiles();

        }
        else{
            mStateView.setText("Disconnected");
        }

    }

    /**
     * @param remoteFileListAdapter
     */
    public void setupRemoteFiles(RemoteFileListAdapter remoteFileListAdapter) {
        mRemoteFileListAdapter = remoteFileListAdapter;
        //  Log.v("Filelistactivity","is remote adapter null? "+(remoteFileListAdapter == null)+", is view null? "+(mRemoteListView==null));
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
            SftpExec exec = new SftpExec(arr, progressDialog);
            new SFTPTask(this, exec, progressDialog).execute();
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

    private void showRemoteFiles(){
        try {
            mSessionController.openChannel(new TaskCallbackHandler() {
                @Override
                public void onFail() {
                    //nothing
                }

                @Override
                public void onTaskFinished(Vector<ChannelSftp.LsEntry> lsEntries) {
                    mRemoteFileListAdapter = new RemoteFileListAdapter(FileListActivity.this, lsEntries);
                    mRemoteListView.setAdapter(mRemoteFileListAdapter);
                    mRemoteFileListAdapter.notifyDataSetChanged();
                }
            }, ""); //(this, mRemoteFileListAdapter, "");
        } catch (JSchException j) {
            Log.v("FILELIST", "OHNO " + j.getMessage() + ", " + j.getCause());
        } catch (SftpException s) {
            Log.v("FILELIST", "SFTPEX: " + s.getMessage() + ", " + s.getCause());
        }
    }

    public class SFTPTask extends AsyncTask<Void, Void, Boolean> {

        private SshExecutor mEx;

        private SftpProgressDialog mProgressDialog;

        //
        // Constructor
        //

        public SFTPTask(Context context, SshExecutor exec, SftpProgressDialog spd) {
            mEx = exec;
            mProgressDialog = spd;

        }

        @Override
        protected void onPreExecute() {

            mProgressDialog.setTitle(getResources().getText(R.string.progress_runningcommand));
            mProgressDialog.setMessage(getResources().getText(R.string.progress_pleasewait));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
            getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            boolean success = upload();

            return success;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            // TODO: if fail explain to user
            mProgressDialog.dismiss();
            getWindow().addFlags(LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }

        /**
         *
         */
        private boolean upload() {

            boolean success = true;

            try {
                mEx.executeCommand(mSessionController.getSession());
            } catch (JSchException e) {
                success = false;
                e.printStackTrace();
            } catch (IOException e) {
                success = false;
                e.printStackTrace();
            }

            return success;
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
         * Data upload is ended.
         */
        public void end() {
            this.setProgress(this.getMax());

        }

        /**
         * Initializes the SftpProgressMonitor
         */
        public void init(int arg0, String arg1, String arg2, long arg3) {
            mSize = arg3;

        }


    }


    private class RemoteClickListener implements OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            if(mIsProcessing){
                return;
            }
            Log.v("FIULELISTACTIVITY", "TRYING TO OPEN DIRECTORY ");
            if (mRemoteFileListAdapter == null) {
                Log.v("FIULELISTACTIVITY", "adapter null?");
                return;
            }
            //Is directory?
            if (mRemoteFileListAdapter.getRemoteFiles().get(position).getAttrs().isDir()
                    || mRemoteFileListAdapter.getRemoteFiles().get(position).getFilename().trim() == "..") {


                try {
                    mIsProcessing = true;
                    mSessionController.openChannel( new TaskCallbackHandler() {
                        @Override
                        public void onFail() {
                            mIsProcessing = false;
                        }

                        @Override
                        public void onTaskFinished(Vector<ChannelSftp.LsEntry> lsEntries) {
                            mRemoteFileListAdapter = new RemoteFileListAdapter(FileListActivity.this, lsEntries);
                            mRemoteListView.setAdapter(mRemoteFileListAdapter);
                            mRemoteFileListAdapter.notifyDataSetChanged();
                            mIsProcessing = false;
                        }
                    }, mRemoteFileListAdapter.getRemoteFiles().get(position).getFilename());
                } catch (JSchException j) {
                    Log.v("FILELIST", "OHNO " + j.getMessage() + ", " + j.getCause());
                } catch (SftpException s) {
                    Log.v("FILELIST", "SFTPEX: " + s.getMessage() + ", " + s.getCause());
                }

            }
            else{

                // sftp the file
                SftpProgressDialog progressDialog = new SftpProgressDialog(FileListActivity.this, 0);
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();

                File[] arr = {mFilenames.get(position)};
                SftpExec exec = new SftpExec(arr, progressDialog);
                FileOutputStream fos = null;
                try{
                    new FileOutputStream(new File(Environment.getExternalStorageDirectory()+"/"));
                }catch(IOException e){

                }
                try{
                    String name = mRemoteFileListAdapter.getRemoteFiles().get(position).getFilename();
                    String out = Environment.getExternalStorageDirectory()+"/"+name;

                    Log.v("FILELISTACTIVITY","DOWNLOAING HERE"+out);
                // exec.downloadFile(FileListActivity.this.mSessionController.getSession(), mRemoteFileListAdapter.getRemoteFiles().get(position).getFilename(),fos,progressDialog);
                mSessionController.downloadFile(mRemoteFileListAdapter.getRemoteFiles().get(position).getFilename(),out,progressDialog);
                }
                catch(JSchException je){

                }
                catch(SftpException se){

                }
            }


        }
    }


}
