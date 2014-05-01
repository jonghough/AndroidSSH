package com.jgh.androidssh.sshutils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jgh.androidssh.adapters.RemoteFileListAdapter;

/**
 * Performs SFTP transfer of files.
 * 
 * @author Jonathan Hough
 * @since 6 Dec 2012
 *
 */
public class SftpExec implements SshExecutor {
    
    
    // the return string (if any)
    private String mReturnString;
    // User session information
    private SessionUserInfo mSessionUserInfo;
    //
    private File[] mFiles;

    private Vector<ChannelSftp.LsEntry> mRemoteFiles = new Vector<ChannelSftp.LsEntry>();
    
    private SftpProgressMonitor mSPM;

    private ChannelSftp mMainChannel    =null;
    //
    //Constructor
    //
    public SftpExec(){

    }
    
    public SftpExec(File[] files, SessionUserInfo sessionUserInfo) {
        mFiles = files;
        mSessionUserInfo = sessionUserInfo;
    }
    
    public SftpExec(File[] files, SessionUserInfo sessionUserInfo, SftpProgressMonitor spm) {
        mFiles = files;
        mSessionUserInfo = sessionUserInfo;
        mSPM = spm;
    }

    public SftpExec(File[] files, SftpProgressMonitor spm) {
        mFiles = files;
        mSPM = spm;
    }


    public Vector<ChannelSftp.LsEntry> openChannel(Session session,Context context, RemoteFileListAdapter fileListAdapter) throws JSchException, SftpException{
        if(session == null || !session.isConnected()){
            session.connect();
        }

        if(true||mMainChannel == null || mMainChannel.isClosed()){
            Channel channel = session.openChannel("sftp");

            mMainChannel = (ChannelSftp)channel;
            mMainChannel.connect();
            mRemoteFiles = mMainChannel.ls("");
            Log.v("SFTPEXEC", "REMOTE FILE SIZE " + mRemoteFiles.size());
            for(ChannelSftp.LsEntry e : mRemoteFiles){

                Log.v("SFTPEXEC"," file "+ e.getFilename());
            }
            fileListAdapter = new RemoteFileListAdapter(context,mRemoteFiles);
        }
        return mRemoteFiles;
    }


    
    /**
     *Creates a connection and sequentially transfers each file.
     */
    public int executeCommand(Session session) throws JSchException, IOException {
        if(session == null || !session.isConnected()){
            return -1;
        }

        Channel channel = session.openChannel("sftp");
        
        channel.setInputStream(null);
        
        channel.connect();
        ChannelSftp channelSftp = (ChannelSftp)channel;
      
       
        for (File file : mFiles) {
            
            try {
                channelSftp.put(file.getPath(), file.getName(),mSPM, ChannelSftp.APPEND);
            } catch (SftpException e) {
                e.printStackTrace();
            }
        }
        
        return 0;
    }
    
    public String getString() {
        return mReturnString;
    }
    
}
