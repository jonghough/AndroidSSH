
package com.jgh.androidssh.sshutils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * A class to allow shell commands to be sent to the remote server,
 * using Jsch ChannelExec channel.
 * 
 * @author Jonathan Hough
 * @since December 2 2012
 */
public class CommandExec implements SshExecutor {

    public static final String TAG = "COMMANDEXEC";
    // the command to send
    private String mCommand;
    // the return string (if any)
    private String mReturnString;

    public CommandExec(SessionUserInfo sessionUserInfo) {
    }
    public CommandExec() {
    }
    /**
     * Sets the command.
     * 
     * @param command
     */
    public void setCommand(String command) {
        mCommand = command;
    }

    /**
     * Opens connection and sends shell command to server. Command output is
     * returned in an inputstream.
     */
    public int executeCommand(Session session) throws JSchException, IOException {
        Log.v(TAG,"EXECUTING COMMAND");
        if(session == null || !session.isConnected()){
            Log.v(TAG,"NULL OR NO CONNECTION");
            return -1;
        }
        Channel channel = session.openChannel("exec");

        ((ChannelExec) channel).setCommand(mCommand);

        channel.setInputStream(null);

        InputStream in = channel.getInputStream();

        channel.connect();

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        while ((line = br.readLine()) != null) {
            stringBuilder.append(line + "\n");// append newline
        }

        in.close();
        br.close();

        if (stringBuilder.length() > 0)
            mReturnString = stringBuilder.toString();

        else
            mReturnString = "...\n";
        Log.v(TAG,"String is "+mReturnString);
        // disconnect
        channel.disconnect();

        return 0;// TODO: Return nonzero for error
    }

    /**
     * Gets the returned string.
     */
    public String getString() {
        return mReturnString;
    }



}
