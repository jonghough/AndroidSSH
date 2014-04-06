package com.jgh.androidssh.sshutils;

import java.io.File;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
/**
 * Created by jon hough on 3/25/14.
 */
public class ScpExec implements SshExecutor {

    // the return string (if any)
    private String mReturnString;
    // User session information
    private SessionUserInfo mSessionUserInfo;
    //
    private File[] mFiles;
    /**
     *
     */
    private String mScpString;

    private Session mSession;

    /**
     *
     */
    public ScpExec(Session session){
        mSession = session;
    }

    private void beginScp(File file){
        String cmd ="scp " +file;
        try{
        Channel channel=mSession.openChannel("exec");
        }catch(JSchException jSchE){
            //TODO
        }
    }

    public int executeCommand(Session session){
        return 0;
    }

    public String getString(){
        return null;
    }
}
