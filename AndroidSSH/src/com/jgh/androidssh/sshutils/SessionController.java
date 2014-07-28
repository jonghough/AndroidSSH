package com.jgh.androidssh.sshutils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import com.jgh.androidssh.adapters.RemoteFileListAdapter;

import java.util.Properties;
import java.util.Vector;

/**
 * Controller for Jsch SSH sessions. All SSH
 * connections are run through this class.
 * <p/>
 * Created by Jon Hough on 3/25/14.
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

    private ShellController mShellController;
    /**
     * Instance
     */
    private static SessionController sSessionController;


    private SessionController() {
    }

    public static SessionController getSessionController() {
        if (sSessionController == null) {
            sSessionController = new SessionController();
        }
        return sSessionController;
    }


    public Session getSession() {
        return mSession;
    }

    /**
     * @param sessionUserInfo The SessionUserInfo to be used by all SSH channels.
     */
    private SessionController(SessionUserInfo sessionUserInfo) {
        mSessionUserInfo = sessionUserInfo;
        connect();
    }

    public void setUserInfo(SessionUserInfo sessionUserInfo) {
        mSessionUserInfo = sessionUserInfo;
    }


    public void connect() {
        if (mSession == null) {
            mThread = new Thread(new SshRunnable());
            mThread.start();
        } else if (!mSession.isConnected()) {
            mThread = new Thread(new SshRunnable());
            mThread.start();
        }
    }


    private class SftpTask extends AsyncTask<Void, Void, Boolean> {
        RemoteFileListAdapter mfileListAdapter;
        Context mContext;
        Vector<ChannelSftp.LsEntry> mRemoteFiles;
        String mPath;

        public SftpTask(Context context, RemoteFileListAdapter fileListAdapter, String path) {

            mfileListAdapter = fileListAdapter;
            mContext = context;
            mPath = path == null ? "" : path + "/";
        }

        @Override
        protected void onPreExecute() {
            if (!mSession.isConnected()) {
                Log.v("SESSIONCONTROLLER", "SESSION IS NOT CONNECTED");
                connect();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;


            try {
                mRemoteFiles = null;
                if (true) {//||mMainChannel == null || mMainChannel.isClosed()){
                    Channel channel = mSession.openChannel("sftp");
                    channel.setInputStream(null);
                    channel.connect();
                    ChannelSftp channelsftp = (ChannelSftp) channel;
                    mRemoteFiles = channelsftp.ls("/" + mPath);
                    if (mRemoteFiles == null) {
                        Log.v("SESSIONCONTROLLER", " remote file list is null");
                    }
                    // Log.v("SFTPEXEC", "REMOTE FILE SIZE " + mRemoteFiles.size());
                    else {
                        for (ChannelSftp.LsEntry e : mRemoteFiles) {

                            Log.v("SFTPEXEC", " file " + e.getFilename());
                        }

                    }
                }
            } catch (Exception e) {
                Log.v("SESSIONCONTROLLER", "sftprunnable exptn " + e.getCause());
                success = false;
                return success;
            }


            return true;
        }


        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mfileListAdapter = new RemoteFileListAdapter(mContext, mRemoteFiles);
                ((FileListActivity) mContext).setupRemoteFiles(mfileListAdapter);
            }
        }
    }

    /**
     * Uploads files to remote server.
     *
     * @param files
     * @param spm
     */
    public void uploadFiles(File[] files, SftpProgressMonitor spm) {
        if (mSftpController == null) {
            mSftpController = new SftpController();

        }
        mSftpController.new UploadTask(mSession, files, spm).execute();
    }


    /**
     * Downloads file from remote server.
     *
     * @param srcPath
     * @param out
     * @param spm
     * @return
     * @throws JSchException
     * @throws SftpException
     */
    public boolean downloadFile(String srcPath, String out, SftpProgressMonitor spm) throws JSchException, SftpException {
        if (mSftpController == null) {
            mSftpController = new SftpController();

        }
        mSftpController.new DownloadTask(mSession, srcPath, out, spm).execute();
        return true;
    }

    /**
     * @param taskCallbackHandler
     * @param path
     * @throws JSchException
     * @throws SftpException
     */
    public void listRemoteFiles(TaskCallbackHandler taskCallbackHandler, String path) throws JSchException, SftpException {

        if (mSession == null || !mSession.isConnected()) {
            return;
        }

        if (mSftpController == null) {
            mSftpController = new SftpController();

        }
        //list the files.
        mSftpController.lsRemoteFiles(mSession, taskCallbackHandler, path);


    }


    /**
     * Disconnects session.
     */
    public void disconnect() {
        if (mSession != null) {
            mSession.disconnect();
        }
        if (mThread != null && mThread.isAlive()) {
            try {
                mThread.join();
            } catch (InterruptedException e) {
                //
            }
        }
    }


    /**
     * Execute command on remote server
     *
     * @param command
     * @return
     */
    public boolean executeCommand(Handler handler, EditText editText, ExecTaskCallbackHandler callback, String command) {
        if (mSession == null || !mSession.isConnected()) {
            return false;
        } else {
            try {
                mShellController = new ShellController(this);
                mShellController.openShell(handler, editText);
            } catch (Exception e) {
            }
            return true;
           /* try {
                if (mChannelExec == null || mChannelExec.isClosed()) {
                    mChannelExec = (ChannelExec) mSession.openChannel("Exec");
                }

                //mChannelExec.setCommand(command);
                CommandExec mComEx = new CommandExec();

                mComEx.setCommand(command);
                new ExecSshTask(context, mComEx, callback).execute();
            } catch (JSchException jex) {
                Log.e("SessionController", "Fail to complete command");
                return false;
            }
            return true;*/
        }
    }


    /**
     *
     */
    public class SshRunnable implements Runnable {

        public void run() {
            JSch jsch = new JSch();
            mSession = null;
            try {
                mSession = jsch.getSession(mSessionUserInfo.getUser(), mSessionUserInfo.getHost(),
                        22); // port 22

                mSession.setUserInfo(mSessionUserInfo);

                Properties properties = new Properties();
                properties.setProperty("StrictHostKeyChecking", "no");
                mSession.setConfig(properties);
                mSession.connect();

            } catch (JSchException jex) {
                Log.e("SessionController", "exptn: " + jex.getMessage() + ", Fail to get session " + mSessionUserInfo.getUser() + ", " + mSessionUserInfo.getHost());
            } catch (Exception ex) {
                Log.e("SessionController", "exptn:" + ex.getMessage());
            }

            Log.v("SessionController", "Session !" + mSession.isConnected());
        }
    }


    public class ExecSshTask extends AsyncTask<Void, Void, Boolean> {

        private SshExecutor mSshExecutor;

        private ExecTaskCallbackHandler mTaskCallbackHandler;

        //
        // Constructor
        //

        public ExecSshTask(Context context, SshExecutor exec, ExecTaskCallbackHandler taskCallbackHandler) {
            mSshExecutor = exec;
            mTaskCallbackHandler = taskCallbackHandler;

        }

        @Override
        protected void onPreExecute() {


        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            boolean success = false;
            try {
                mSshExecutor.executeCommand(getSession());
            } catch (JSchException e) {
                if (mTaskCallbackHandler != null)
                    mTaskCallbackHandler.onFail();
                // makeToast(R.string.taskfail);
            } catch (IOException e) {
                if (mTaskCallbackHandler != null)
                    mTaskCallbackHandler.onFail();
            }
            success = true;
            return success;
        }

        @Override
        protected void onPostExecute(Boolean b) {

            if (b) {
                if (mTaskCallbackHandler != null)
                    mTaskCallbackHandler.onComplete(mSshExecutor.getString() + "\n");

            } else {
                if (mTaskCallbackHandler != null)
                    mTaskCallbackHandler.onFail();
            }

        }
    }


    /**
     * Opens shell channel with the current sesison and opens input and output streams with remote server.
     * This method is only used for testing. Remove later.
     * @throws JSchException
     * @throws IOException
     */
    public void openShell() throws JSchException, IOException {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Channel channel = mSession.openChannel("shell");
                    channel.connect();

                    BufferedReader input;
                    input = new BufferedReader(new InputStreamReader(channel.getInputStream()));
                    DataOutputStream output = new DataOutputStream(channel.getOutputStream());


                    output.writeBytes("ls\r\n");
                    output.flush();


                    String line = input.readLine();
                    String result = line + "\n";

                    while ((line = input.readLine()) != null) {
                        result += line + "\n";
                        Log.v("TAG", "Line: " + line);
                    }
                } catch (Exception e) {
                    Log.v("EXECPTION", " EX " + e.getMessage());
                }

            }
        }).start();
    }

}
