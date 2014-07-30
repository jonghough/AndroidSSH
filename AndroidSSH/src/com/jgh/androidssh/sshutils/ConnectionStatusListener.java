package com.jgh.androidssh.sshutils;

/**
 * Created by Jon Hough 7/31/14.
 */
public interface ConnectionStatusListener {

   public void onDisconnected();

   public void onConnected();
}
