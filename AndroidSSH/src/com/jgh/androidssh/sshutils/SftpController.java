package com.jgh.androidssh.sshutils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jgh.androidssh.FileListActivity;
import com.jgh.androidssh.adapters.RemoteFileListAdapter;

import java.io.FileOutputStream;
import java.util.Vector;

/**
 * Created by Jon Hough on 4/19/14.
 */
public class SftpController {

    public static final String TAG = "SftpController";

    private String mCurrentPath;

    public SftpController(){

    }

    public SftpController(String path){
        mCurrentPath = path;
    }


    public void resetPathToRoot(){
        mCurrentPath = "";
    }

    public String getPath(){
        return mCurrentPath;
    }

    public void setPath(String path){
        mCurrentPath = path;
    }

    public void appendToPath(String relPath){
        if(mCurrentPath == null){
            mCurrentPath = relPath;
        }
        else mCurrentPath += relPath;
    }

    public void lsRemoteFiles(Session session,TaskCallbackHandler taskCallbackHandler, String path){
        mCurrentPath = path == null ? "": path+"/";
        new LsTask(session, taskCallbackHandler).execute();
    }



    /**
     *Shows all files (command ls) including directories.
     */
    private class LsTask extends AsyncTask<Void,Void,Boolean> {
        RemoteFileListAdapter mfileListAdapter;
        Context mContext;
        Vector<ChannelSftp.LsEntry> mRemoteFiles;
        TaskCallbackHandler mTaskCallbackHandler;

        private Session mSession;
        public LsTask(Session session, TaskCallbackHandler tch){

            mSession                = session;
            mTaskCallbackHandler    = tch;
        }

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;

            Log.v(TAG, "current path is ..................... "+mCurrentPath);

            Channel channel = null;
            try{
                mRemoteFiles = null;
                if(true){//||mMainChannel == null || mMainChannel.isClosed()){
                    channel = mSession.openChannel("sftp");
                    channel.setInputStream(null);
                    channel.connect();
                    ChannelSftp channelsftp = (ChannelSftp)channel;
                    mRemoteFiles = channelsftp.ls("/"+mCurrentPath);
                    if(mRemoteFiles ==null){
                        Log.v(TAG," remote file list is null");
                    }
                    // Log.v("SFTPEXEC", "REMOTE FILE SIZE " + mRemoteFiles.size());
                    else{
                        for(ChannelSftp.LsEntry e : mRemoteFiles){

                            Log.v(TAG," file "+ e.getFilename());
                        }

                    }
                }
            }
            catch(Exception e){
                Log.v(TAG, "sftprunnable exptn "+e.getCause());
                success = false;
                return success;
            }
            if(channel != null){
                channel.disconnect();
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
                if(mTaskCallbackHandler != null){
                    mTaskCallbackHandler.onTaskFinished(mRemoteFiles);
                }
            }
            else{
                if(mTaskCallbackHandler != null){
                    mTaskCallbackHandler.onFail();
                }
            }
        }
    }

    public void downloadFile(Session session, String srcPath, String out, SftpProgressMonitor spm) throws JSchException, SftpException {
        if(session == null || !session.isConnected()){
            session.connect();
        }

        Channel channel = session.openChannel("sftp");
        ChannelSftp sftpChannel = (ChannelSftp)channel;
        sftpChannel.connect();

        sftpChannel.get(srcPath,out , spm, ChannelSftp.OVERWRITE);


    }

    /**
     * Task for downloading remote file (sftp get).
     */
    public class DownloadTask extends AsyncTask<Void,Void,Boolean> {

        Session mSession;
        String mSrcPath;
        String mOut;
        SftpProgressMonitor mSpm;
        public DownloadTask(Session session, String srcPath, String out, SftpProgressMonitor spm){
            mSession = session;
            mSrcPath = srcPath;
            mOut = out;
            mSpm = spm;
        }

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                Log.v(TAG," path "+mSrcPath);
            downloadFile(mSession, mSrcPath, mOut,mSpm);
            }
            catch(Exception e){
               Log.v(TAG,"EXCEPTION "+e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
                //Need to close progress dialog.

            }
        }
    }

    public class GetTask extends AsyncTask<Void,Void,Boolean> {
        RemoteFileListAdapter mfileListAdapter;
        Context mContext;
        Vector<ChannelSftp.LsEntry> mRemoteFiles;
        TaskCallbackHandler mTaskCallbackHandler;
        SftpProgressMonitor mSftpProgressMonitor;

        private Session mSession;
        public GetTask(Session session, TaskCallbackHandler tch, SftpProgressMonitor sftpProgressMonitor){

            mSession                = session;
            mTaskCallbackHandler    = tch;
            mSftpProgressMonitor = sftpProgressMonitor;
        }

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;

            Log.v(TAG, "current path is ..................... "+mCurrentPath);

            Channel channel = null;
            try{
                mRemoteFiles = null;
                if(true){//||mMainChannel == null || mMainChannel.isClosed()){
                    channel = mSession.openChannel("sftp");
                    channel.setInputStream(null);
                    channel.connect();
                    ChannelSftp channelsftp = (ChannelSftp)channel;
                    channelsftp.get(mCurrentPath, mSftpProgressMonitor);


                }
            }
            catch(Exception e){
                Log.v(TAG, "sftprunnable exptn "+e.getCause());
                success = false;
                return success;
            }
            if(channel != null){
                channel.disconnect();
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
                if(mTaskCallbackHandler != null){
                    mTaskCallbackHandler.onTaskFinished(mRemoteFiles);
                }
            }
            else{
                if(mTaskCallbackHandler != null){
                    mTaskCallbackHandler.onFail();
                }
            }
        }
    }

}
