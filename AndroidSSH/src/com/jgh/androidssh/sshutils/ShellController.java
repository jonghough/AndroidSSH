package com.jgh.androidssh.sshutils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jon on 5/17/14.
 */
public class ShellController {


    private SessionController mSessionController;
    private InputStream mIS;
    private OutputStream mOS;

    public ShellController(SessionController sessionController){
        mSessionController = sessionController;
    }



    private class ShellRunnable implements Runnable{

        private Channel mChannel;
        private InputStream mIS;
        private OutputStream mOS;
        @Override
        public void run(){
            try{
                mChannel= ShellController.this.mSessionController.getSession().openChannel("shell");
                mChannel.setInputStream(mIS);

            }
            catch(JSchException je){

            }
        }
    }

}

