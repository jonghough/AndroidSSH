package com.jgh.androidssh.sshutils;

import com.jcraft.jsch.ChannelSftp;

import java.util.Vector;

/**
 * Interface for remote directory listings (POSIX ls command). Contains callbacks to
 * implement functionality for starting and waiting for remote
 * listing process to finish.
 * Created by Jon Hough on 4/19/14.
 */
public interface TaskCallbackHandler {

    /**
     * Called when remote process begins.
     */
    public void OnBegin();

    /**
     * Called when remote process fails.
     */
    public void onFail();

    /**
     * Called when remote process is completed.
     * @param lsEntries Vector of JSch LsEntry items, the contents of the remote current directory.
     */
    public void onTaskFinished(Vector<ChannelSftp.LsEntry> lsEntries);
}
