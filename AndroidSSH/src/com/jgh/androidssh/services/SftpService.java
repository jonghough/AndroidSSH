package com.jgh.androidssh.services;

import java.io.File;
import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jgh.androidssh.sshutils.SessionUserInfo;
import com.jgh.androidssh.sshutils.SftpExec;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;


public class SftpService extends IntentService {
    
    public SftpService(){
        super("SftpService");
    }

    public SftpService(String name) {
        super("SftpService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        
        Bundle bundle = intent.getExtras();
        String[] fileArray = bundle.getStringArray("Files");
        String[] userInfo = bundle.getStringArray("UserInfo");
       
        File[] files = new File[fileArray.length];
        
        for(int i=0; i<fileArray.length; i++){
            files[i] = new File(fileArray[i]);
        }
        
        SessionUserInfo sui= new SessionUserInfo(userInfo[0], userInfo[1], userInfo[2]);
        
        SftpExec com = new SftpExec(files, sui);
        try {
            com.executeCommand();
        } catch (JSchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
