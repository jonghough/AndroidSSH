package com.jgh.androidssh.sshutils;

import com.jcraft.jsch.UserInfo;

/**
 * A class to hold information used to make JSch session connections.
 * @author Jonathan Hough
 * @since Dec 4 2012
 *
 */
public class SessionUserInfo implements UserInfo {

    private final String mPassword;
    
    private final String mUser;
    
    private final String mHost;
    
    //
    // Constructor
    //
    public SessionUserInfo(String user, String host, String password){
        
        mUser = user;
        mHost = host;
        mPassword = password;
    }
    
    public String getPassphrase() {
        // TODO 
        return null;
    }

    
    
    public String getUser() {
        return mUser;
    }
    
    public String getHost() {
        return mHost;
    } 

    public String getPassword() {
        return mPassword;
    }

    public boolean promptPassphrase(java.lang.String arg0) {
        // TODO 
        return true;
    }

    public boolean promptPassword(java.lang.String arg0) {
        // TODO 
        return true;
    }

    public boolean promptYesNo(java.lang.String arg0) {
        // TODO Auto-generated method stub
        return true;
    }

    public void showMessage(java.lang.String arg0) {
        // TODO Auto-generated method stub
        
    }

   
}
