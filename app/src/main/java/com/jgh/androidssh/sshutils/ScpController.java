package com.jgh.androidssh.sshutils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Created by jon on 8/5/14.
 * unused class.
 */
public class ScpController {


    /**
     * Sends local file to remote server by SCP protocol.
     * @param session The SSH session
     * @param filePath Path to local file to be SCP'd to remote.
     * @throws JSchException
     */
    public static void toFile(Session session, String filePath) throws JSchException {
        if(filePath == null || filePath == "") throw new NullPointerException("File path cannot be null");
        if(session == null ) throw new NullPointerException("Session cannot be null");

        String command = "scp -t "+filePath;

        Channel channel = session.openChannel("exec");
        ((ChannelExec)channel).setCommand(command);
        //TODO scp file...
    }

}

