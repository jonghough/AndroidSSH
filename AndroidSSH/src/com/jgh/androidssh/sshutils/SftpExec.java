package com.jgh.androidssh.sshutils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

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
    
    private SftpProgressMonitor mSPM;
    //
    //Constructor
    //
    
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
