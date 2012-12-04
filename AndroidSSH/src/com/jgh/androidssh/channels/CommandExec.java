package com.jgh.androidssh.channels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * A class to allow shell commands to be sent to the remote server.
 * 
 * @author Jonathan Hough
 * @since December 2 2012
 */
public class CommandExec implements SshExecutor {
    
    // the command to send
    private String mCommand;
    // the return string (if any)
    private String mReturnString;
    // User session information
    private SessionUserInfo mSessionUserInfo;
    
    public CommandExec(String command, SessionUserInfo sessionUserInfo) {
        mCommand = command;
        mSessionUserInfo = sessionUserInfo;
    }
    
    /**
     * Opens connection and sends shell command to server. Command output is
     * returned in an inputstream.
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
        Log.v(mSessionUserInfo.getUser() + "/", mSessionUserInfo.getHost() + "");
        
        session.setUserInfo(mSessionUserInfo);
        
        Properties properties = new Properties();
        properties.setProperty("StrictHostKeyChecking", "no");
        session.setConfig(properties);
        
        // connect
        session.connect();
        
        Channel channel = session.openChannel("exec");
        
        ((ChannelExec)channel).setCommand(mCommand);
        
        channel.setInputStream(null);
        
        InputStream in = channel.getInputStream();
        
        channel.connect();
        
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        
        while ((line = br.readLine()) != null) {
            stringBuilder.append(line+"\n");//append newline
        }
        
        in.close();
        br.close();
        
        if (stringBuilder.length() > 0)
            mReturnString = stringBuilder.toString();
        
        else
            mReturnString = "...\n";
        
        // disconnect
        channel.disconnect();
        session.disconnect();
        
        return 0;// TODO: Return nonzero for error
    }
    
    /**
     * Gets the returned string.
     */
    public String getString() {
        return mReturnString;
    }
    
}
