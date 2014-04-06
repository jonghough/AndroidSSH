package com.jgh.androidssh.sshutils;

import android.util.Log;

import java.io.File;
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
import java.util.Properties;
/**
 * Created by jon on 3/25/14.
 */
public class SessionController {

    /**
     *
     */
    private Session mSession;
    private SessionUserInfo mSessionUserInfo;
    private ChannelExec mChannelExec;
    private Thread mThread;
    /**
     *
     * @param sessionUserInfo The SessionUserInfo to be used by all SSH channels.
     */
    public SessionController(SessionUserInfo sessionUserInfo){
        mSessionUserInfo = sessionUserInfo;
        connect();
    }


    public Session getSession(){
        return mSession;
    }


    public void connect(){
        disconnect();
        mThread = new Thread(new SshRunnable());
        mThread.start();
    }

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
