package com.jgh.androidssh.sshutils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jgh.androidssh.FileListActivity;
import com.jgh.androidssh.adapters.RemoteFileListAdapter;

import java.util.Vector;

/**
 * Created by jon on 4/19/14.
 */
public class SftpController {

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
     *
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

            Log.v("sftpcontroller", "current path is ..................... "+mCurrentPath);


            try{
                mRemoteFiles = null;
                if(true){//||mMainChannel == null || mMainChannel.isClosed()){
                    Channel channel = mSession.openChannel("sftp");
                    channel.setInputStream(null);
                    channel.connect();
                    ChannelSftp channelsftp = (ChannelSftp)channel;
                    mRemoteFiles = channelsftp.ls("/"+mCurrentPath);
                    if(mRemoteFiles ==null){
                        Log.v("SESSIONCONTROLLER"," remote file list is null");
                    }
                    // Log.v("SFTPEXEC", "REMOTE FILE SIZE " + mRemoteFiles.size());
                    else{
                        for(ChannelSftp.LsEntry e : mRemoteFiles){

                            Log.v("SFTPEXEC"," file "+ e.getFilename());
                        }

                    }
                }
            }
            catch(Exception e){
                Log.v("SESSIONCONTROLLER", "sftprunnable exptn "+e.getCause());
                success = false;
                return success;
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
