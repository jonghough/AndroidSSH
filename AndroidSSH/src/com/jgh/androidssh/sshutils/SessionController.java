package com.jgh.androidssh.sshutils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.lang.InterruptedException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jgh.androidssh.FileListActivity;
import com.jgh.androidssh.R;
import com.jgh.androidssh.adapters.FileListAdapter;
import com.jgh.androidssh.adapters.RemoteFileListAdapter;

import java.util.Properties;
import java.util.Vector;

/**
 * Created by jon on 3/25/14.
 */
public class SessionController {

    /**
     *
     */
    private Session mSession;
    /**
     *
     */
    private SessionUserInfo mSessionUserInfo;
    /**
     *
     */
    private ChannelExec mChannelExec;
    /**
     *
     */
    private Thread mThread;
    /**
     *
     */
    private SftpController mSftpController;

    private static SessionController sSessionController;


    private SessionController(){
    }

    public static SessionController getSessionController(){
        if(sSessionController == null){
            sSessionController = new SessionController();
        }
        return sSessionController;
    }



    public Session getSession(){
        return mSession;
    }

    /**
     *
     * @param sessionUserInfo The SessionUserInfo to be used by all SSH channels.
     */
    private SessionController(SessionUserInfo sessionUserInfo){
        mSessionUserInfo = sessionUserInfo;
        connect();
    }

    public void setUserInfo(SessionUserInfo sessionUserInfo){
        mSessionUserInfo = sessionUserInfo;
    }



    public void connect(){
        if(mSession == null){
            mThread = new Thread(new SshRunnable());
            mThread.start();
        }
        else if(!mSession.isConnected()){
            mThread = new Thread(new SshRunnable());
            mThread.start();
        }
    }





    private class SftpTask extends AsyncTask<Void,Void,Boolean>{
        RemoteFileListAdapter mfileListAdapter;
        Context mContext;
        Vector<ChannelSftp.LsEntry> mRemoteFiles;
        String mPath;
        public SftpTask(Context context, RemoteFileListAdapter fileListAdapter, String path){

            mfileListAdapter    = fileListAdapter;
            mContext            = context;
            mPath               = path == null ? "": path+"/";
        }

        @Override
        protected void onPreExecute(){
            if(!mSession.isConnected()){
                Log.v("SESSIONCONTROLLER","SESSION IS NOT CONNECTED");
                connect();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;


            try{
                mRemoteFiles = null;
                if(true){//||mMainChannel == null || mMainChannel.isClosed()){
                    Channel channel = mSession.openChannel("sftp");
                    channel.setInputStream(null);
                    channel.connect();
                    ChannelSftp channelsftp = (ChannelSftp)channel;
                    mRemoteFiles = channelsftp.ls("/"+mPath);
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
                mfileListAdapter = new RemoteFileListAdapter(mContext,mRemoteFiles);
                ((FileListActivity)mContext).setupRemoteFiles(mfileListAdapter);
            }
        }
    }


    public boolean downloadFile(String srcPath, String out, SftpProgressMonitor spm) throws JSchException, SftpException{
        if(mSftpController == null){
            mSftpController = new SftpController();

        }
        mSftpController. new DownloadTask(mSession, srcPath,out,spm).execute();
        return true;
    }
    /**
     *
     * @param taskCallbackHandler
     * @param path
     * @throws JSchException
     * @throws SftpException
     */
    public void openChannel(TaskCallbackHandler taskCallbackHandler, String path) throws JSchException, SftpException{

        if(mSession == null || !mSession.isConnected()){
            return;
        }

        if(mSftpController == null){
            mSftpController = new SftpController();

        }
        //list the files.
        mSftpController.lsRemoteFiles(mSession, taskCallbackHandler, path);


    }


    /**
     *
     */
    public void disconnect(){
        if(mSession != null){
            mSession.disconnect();
        }
        if(mThread != null && mThread.isAlive()){
            try{
                mThread.join();
            }
            catch(InterruptedException e){
                //
            }
        }
    }


    /**
     *
     * @param command
     * @return
     */
    public boolean executeCommand(String command){
        if(mSession == null || !mSession.isConnected()){
            return false;
        }

        else{
            try{
                if(mChannelExec == null || mChannelExec.isClosed()){
                    mChannelExec =  (ChannelExec)mSession.openChannel("Exec");
                }

                mChannelExec.setCommand(command);
            }
            catch(JSchException jex){
                Log.e("SessionController", "Fail to do command");
                return false;
            }
            return true;
        }
    }

    public void openShell(InputStream in, OutputStream out){

        try{
            Channel channel = mSession.openChannel("shell");
            channel.setInputStream(in);
            channel.setOutputStream(out);
            channel.connect();
         }
        catch(JSchException jex){
            Log.e("SessionController", "Fail to do command");

        }
    }


    public class SshRunnable implements Runnable{

        public void run(){
            JSch jsch = new JSch();
            mSession = null;
            try{
                mSession = jsch.getSession(mSessionUserInfo.getUser(), mSessionUserInfo.getHost(),
                        22); // port 22

                mSession.setUserInfo(mSessionUserInfo);

                Properties properties = new Properties();
                properties.setProperty("StrictHostKeyChecking", "no");
                mSession.setConfig(properties);
                mSession.connect();
            }
            catch(JSchException jex){
                Log.e("SessionController", "exptn: "+jex.getMessage()+", Fail to get session "+mSessionUserInfo.getUser()+", "+ mSessionUserInfo.getHost());
            }


            Log.v("SessionController","Session !"+mSession.isConnected());
        }
    }

}
