package com.jgh.androidssh.sshutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

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
    
    //
    //Constructor
    //
    
    public SftpExec(File[] files, SessionUserInfo sessionUserInfo) {
        mFiles = files;
        mSessionUserInfo = sessionUserInfo;
    }
    
    /**
     *Creates a connection and sequentially transfers each file.
     */
    public int executeCommand() throws JSchException, IOException {
        
        JSch jsch = new JSch();
        
        // Start session
        Session session = null;
        try {
            session = jsch.getSession(mSessionUserInfo.getUser(), mSessionUserInfo.getHost(), 22); // port
                                                                                                   // 22
        } catch (JSchException jschE) {
            throw new JSchException("Failed to get session.");
        }
        
        session.setUserInfo(mSessionUserInfo);
        session.getUserInfo().getPassword();
        Properties properties = new Properties();
        properties.setProperty("StrictHostKeyChecking", "no");
        session.setConfig(properties);
        
        // connect
        session.connect();
        
        Channel channel = session.openChannel("sftp");
        
        channel.setInputStream(null);
        
        channel.connect();
        ChannelSftp channelSftp = (ChannelSftp)channel;
    
        
        for (File file : mFiles) {
            
            try {
              
                channelSftp.put(new FileInputStream(file), file.getName());
                
               
            } catch (SftpException e) {
                
                e.printStackTrace();
            }
        }
        
        
        channel.disconnect();
        session.disconnect();
        
        return 0;
    }
    
    public String getString() {
        return mReturnString;
    }
    
}
