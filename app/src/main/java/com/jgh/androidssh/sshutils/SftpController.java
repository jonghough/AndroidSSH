package com.jgh.androidssh.sshutils;

import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * Controller class for SFTP functions. Performs SFTP
 * ls, get, put commands between local device and remote SSH
 * server. For each process a new sftpchannel is opened and closed
 * after completion.
 */
public class SftpController {

    /**
     * Tag name
     */
    public static final String TAG = "SftpController";

    /**
     * Remote directory path. The path to the current remote directory.
     */
    private String mCurrentPath = "/";


    /**
     * Creates instance of SftpController. Performs SFTP functions.
     */
    public SftpController() {

    }

    /**
     * Creates instance of SftpController. Performs SFTP functions.
     *
     * @param path path to chosen directory on remote host.
     */
    public SftpController(String path) {
        mCurrentPath = path;
    }


    /**
     * Resets the current path on remote server.
     */
    public void resetPathToRoot() {
        mCurrentPath = "/";
    }


    /**
     * Returns the path to the current directory on the
     * remote server.
     *
     * @return
     */
    public String getPath() {
        return mCurrentPath;
    }


    /**
     * Sets the path to current directory on the remote
     * server.
     *
     * @param path
     */
    public void setPath(String path) {
        mCurrentPath = path;
    }


    /**
     * Appends <b>relPath</b> to the current path on remote host.
     *
     * @param relPath relative path
     */
    public void appendToPath(String relPath) {
        if (mCurrentPath == null) {
            mCurrentPath = relPath;
        } else mCurrentPath += relPath;
    }


    /**
     * Disconnects SFTP.
     */
    public void disconnect() {
        //nothing yet
    }


    /**
     * Upload file(s) Task. Aysnc task for uploading local files to remote
     * server.
     */
    public class UploadTask extends AsyncTask<Void, Void, Boolean> {

        /**
         * JSch Session Instance
         */
        private Session mSession;

        /**
         * Progress dialog to monitor upload progress.
         */
        private SftpProgressMonitor mProgressDialog;

        /**
         * Array of local files to be uploaded
         */
        private File[] mLocalFiles;

        //
        // Constructor
        //

        public UploadTask(Session session, File[] localFiles, SftpProgressMonitor spd) {

            mProgressDialog = spd;
            mLocalFiles = localFiles;
            mSession = session;
        }

        @Override
        protected void onPreExecute() {
            //nothing
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            boolean success = true;
            try {
                uploadFiles(mSession, mLocalFiles, mProgressDialog);
            } catch (JSchException e) {
                e.printStackTrace();
                Log.e(TAG, "JSchException " + e.getMessage());
                success = false;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "IOException " + e.getMessage());
                success = false;
            } catch (SftpException e) {
                e.printStackTrace();
                Log.e(TAG, "SftpException " + e.getMessage());
                success = false;
            } finally {
                return success;
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            //TODO
        }

    }


    /**
     * Uploads the files in <b>localFiles</b> to the current directory on
     * remote server.
     *
     * @param session    the Jsch SSH session instance
     * @param localFiles array of files to be uploaded
     * @param spm        progress monitor
     * @throws JSchException
     * @throws java.io.IOException
     * @throws SftpException
     */
    public void uploadFiles(Session session, File[] localFiles, SftpProgressMonitor spm) throws JSchException, IOException, SftpException {
        if (session == null || !session.isConnected()) {
            session.connect();
        }

        Channel channel = session.openChannel("sftp");
        channel.setInputStream(null);
        channel.connect();
        ChannelSftp channelSftp = (ChannelSftp) channel;

        for (File file : localFiles) {
            channelSftp.put(file.getPath(), file.getName(), spm, ChannelSftp.APPEND);
        }

        channelSftp.disconnect();
    }

    /**
     * SHell ls command to list remote host's file list for the given directory.
     *
     * @param session             the session
     * @param taskCallbackHandler handle ls success or failure
     * @param path                relative path from current directory.
     */
    public void lsRemoteFiles(Session session, TaskCallbackHandler taskCallbackHandler, String path) {
        mCurrentPath = path == null || path == "" ? mCurrentPath : mCurrentPath + path + "/";
        new LsTask(session, taskCallbackHandler).execute();
    }


    /**
     * Shows all files (command ls) including directories.
     */
    private class LsTask extends AsyncTask<Void, Void, Boolean> {

        /**
         * Vector of files in the remote server's current
         * directory.
         */
        private Vector<ChannelSftp.LsEntry> mRemoteFiles;

        /**
         * Callback handler for task completion ot failure.
         */
        private TaskCallbackHandler mTaskCallbackHandler;

        /**
         * JSch session instance.
         */
        private Session mSession;


        /**
         * Async Task for listing contents of remote directory.
         *
         * @param session currently connected Jsch session.
         * @param tch     callback handler for completion or failure.
         */
        public LsTask(Session session, TaskCallbackHandler tch) {

            mSession = session;
            mTaskCallbackHandler = tch;
        }

        @Override
        protected void onPreExecute() {
            if(mTaskCallbackHandler != null)
                mTaskCallbackHandler.OnBegin();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Log.d(TAG, "current path is " + mCurrentPath);

            boolean success = true;
            Channel channel = null;
            try {
                mRemoteFiles = null;
                if (true) {
                    channel = mSession.openChannel("sftp");
                    channel.setInputStream(null);
                    channel.connect();
                    ChannelSftp channelsftp = (ChannelSftp) channel;
                    String path = mCurrentPath == null ? "/" : mCurrentPath;
                    mRemoteFiles = channelsftp.ls(path);
                    if (mRemoteFiles == null) {
                        Log.d(TAG, "remote file list is null");
                    } else {
                        for (ChannelSftp.LsEntry e : mRemoteFiles) {
                            //Log.v(TAG," file "+ e.getFilename());
                        }
                    }
                }
            } catch (Exception e) {
                Log.v(TAG, "sftprunnable exptn " + e.getCause());
                success = false;
                return success;
            }
            if (channel != null) {
                channel.disconnect();
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                if (mTaskCallbackHandler != null) {
                    mTaskCallbackHandler.onTaskFinished(mRemoteFiles);
                }
            } else {
                if (mTaskCallbackHandler != null) {
                    mTaskCallbackHandler.onFail();
                }
            }
        }
    }


    /**
     * Downloads the remote file at srcPath location on remote host, to the out
     * location on local device.
     * Uses SFTP get function.
     *
     * @param session
     * @param srcPath
     * @param out
     * @param spm
     * @throws JSchException
     * @throws SftpException
     */
    public void downloadFile(Session session, String srcPath, String out, SftpProgressMonitor spm) throws JSchException, SftpException {
        if (session == null || !session.isConnected()) {
            session.connect();
        }

        Channel channel = session.openChannel("sftp");
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.connect();
        sftpChannel.get(srcPath, out, spm, ChannelSftp.OVERWRITE);
        sftpChannel.disconnect();
    }

    /**
     * Async Task for downloading remote file (sftp get command).
     */
    public class DownloadTask extends AsyncTask<Void, Void, Boolean> {

        Session mSession;
        String mSrcPath;
        String mOut;
        SftpProgressMonitor mSpm;

        /**
         * Async Task for downloading remote file to local device (SFTP get command).
         *
         * @param session Current connected JSch Session
         * @param srcPath Path to target file on remote server
         * @param out     Path to output location on local device.
         * @param spm     Progress monitor, to monitor download progress.
         */
        public DownloadTask(Session session, String srcPath, String out, SftpProgressMonitor spm) {
            mSession = session;
            mSrcPath = srcPath;
            mOut = out;
            mSpm = spm;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean result = false;
            try {
                Log.v(TAG, " path " + mSrcPath + ", out path " + mOut);
                downloadFile(mSession, mCurrentPath + mSrcPath, mOut, mSpm);
                result = true;

            } catch (Exception e) {
                Log.e(TAG, "EXCEPTION " + e.getMessage());

            } finally {
                return result;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success == null) return;
            if (success) {
                mSpm.end();
            }
        }
    }
}
