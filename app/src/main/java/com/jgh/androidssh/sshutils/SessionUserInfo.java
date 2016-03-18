package com.jgh.androidssh.sshutils;

import com.jcraft.jsch.UserInfo;

/**
 * A class to hold information used to make JSch session connections.
 * Implements JSch library's UserInfo interface. Used for opening Session
 * connection using username, password and host address information.
 *
 * @author Jonathan Hough
 */
public class SessionUserInfo implements UserInfo {

    /**
     * User password.
     */
    private final String mPassword;

    /**
     * User name
     */
    private final String mUser;

    /**
     * Host address
     */
    private final String mHost;


    /**
     * Host port
     */
    private final int mPort;

    //
    // Constructor
    //

    /**
     * Creates an instance of SessionUserInfo with the user name, host name,
     * user password and port number, to gain SSH access to remote host.
     *
     * @param user     username for SSH access to remote server
     * @param host     remote server host name
     * @param password password to access server
     * @param port     port number
     */
    public SessionUserInfo(String user, String host, String password, int port) {

        mUser = user;
        mHost = host;
        mPassword = password;
        mPort = port;
    }

    public String getPassphrase() {
        // TODO 
        return null;
    }


    /**
     * Gets the username for SSH session login.
     * @return username
     */
    public String getUser() {
        return mUser;
    }

    /**
     * Gets the remote host name, or IP address.
     * @return host name, or IP address
     */
    public String getHost() {
        return mHost;
    }

    /**
     * Gets the user password for SSH login to server.
     * @return SSH password
     */
    public String getPassword() {
        return mPassword;
    }

    /**
     * Gets the port number of the remote server for SSH access.
     * Usually this is port 22.
     * @return port number
     */
    public int getPort(){ return mPort;}

    public boolean promptPassphrase(String arg0) {
        // TODO 
        return true;
    }

    public boolean promptPassword(String arg0) {
        // TODO 
        return true;
    }

    public boolean promptYesNo(String arg0) {
        // TODO Auto-generated method stub
        return true;
    }

    public void showMessage(String arg0) {
        // TODO Auto-generated method stub

    }


}
