package com.jgh.androidssh.sshutils;

import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by jon on 5/17/14.
 */
public class ShellController {


    private final SessionController mSessionController;
    private BufferedReader mBufferedReader;
    private DataOutputStream mDataOutputStream;

    public ShellController(SessionController sessionController){
        mSessionController = sessionController;
    }



    public void openShell(Handler handler, EditText editText) throws JSchException, IOException {
        final Handler myHandler = handler;
        final EditText myEditText = editText;
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Channel channel = mSessionController.getSession().openChannel("shell");
                    channel.connect();

                    BufferedReader input;
                    input = new BufferedReader(new InputStreamReader(channel.getInputStream()));
                    DataOutputStream output = new DataOutputStream(channel.getOutputStream());


                    output.writeBytes("ls"+"\r\n");
                    output.flush();


                    String line = input.readLine();
                    String result = line + "\n";

                    while ((line = input.readLine()) != null) {
                        result += line + "\n";
                        final String rez = line;

                        myHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                myEditText.setText(myEditText.getText().toString() + "\n" + rez);
                            }
                        });
                    }

                } catch (Exception e) {
                    Log.v("EXECPTION", " EX " + e.getMessage()+"."+e.getCause()+","+e.getClass().toString());
                }

            }
        }).start();
    }

}

