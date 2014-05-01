package com.jgh.androidssh.sshutils;

import com.jcraft.jsch.ChannelSftp;

import java.util.Vector;

/**
 * Created by jon on 4/19/14.
 */
public interface TaskCallbackHandler {

    public void onFail();

    public void onTaskFinished(Vector<ChannelSftp.LsEntry> lsEntries);
}
