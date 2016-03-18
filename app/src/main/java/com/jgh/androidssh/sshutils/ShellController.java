package com.jgh.androidssh.sshutils;

import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jgh.androidssh.SshEditText;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Controller for SSH shell-like process between local device and remote SSH server.
 * Sustains an open channel to remote server and streams data between local device
 * and remote.
 * <p/>
 * Created by Jon Hough on 5/17/14.
 */
public class ShellController {

    public static final String TAG = "ShellController";
    /**
     *
     */
    private BufferedReader mBufferedReader;

    /**
     *
     */
    private DataOutputStream mDataOutputStream;

    /**
     *
     */
    private Channel mChannel;

    /**
     *
     */
    private String mSshText = null;


    public ShellController() {
        //nothing
    }


    /**
     * Gets the dataoutputstream
     * (IO stream to remote server)
     * @return dataOutputStream
     */
    public DataOutputStream getDataOutputStream() {
        return mDataOutputStream;
    }


    /**
     * Disconnects shell and closes streams.
     *
     * @throws java.io.IOException
     */
    public synchronized void disconnect() throws IOException {
        try {
            Log.v(TAG, "close shell channel");
            //disconnect channel
            if (mChannel != null)
                mChannel.disconnect();

            Log.v(TAG, "close streams");
            //close streams
            mDataOutputStream.flush();
            mDataOutputStream.close();
            mBufferedReader.close();

        }catch(Throwable t){
            Log.e(TAG, "Exception: "+t.getMessage());
        }
    }

    /**
     * Writes to the outputstream, to remote server. Input should ideally come from an EditText, to which
     * the shell response output will also be written, to simulate a shell terminal.
     *
     * @param command commands string.
     */
    public void writeToOutput(String command) {
        if (mDataOutputStream != null) {
            try {
                mDataOutputStream.writeBytes(command + "\r\n");
                mDataOutputStream.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Opens shell connection to remote server. Listens for user input in EditText Data Input Stream and
     * streams to remote server. Server responses are streamed back on background thread and
     * output to the EditText.
     *
     * @param handler  Handler for updating UI EditText on non-UI thread.
     * @param editText EditText to act as input and output point.
     * @throws JSchException
     * @throws java.io.IOException
     */
    public void openShell(Session session, Handler handler, EditText editText) throws JSchException, IOException {
        if (session == null) throw new NullPointerException("Session cannot be null!");
        if (!session.isConnected()) throw new IllegalStateException("Session must be connected.");
        final Handler myHandler = handler;
        final EditText myEditText = editText;
        mChannel = session.openChannel("shell");
        mChannel.connect();
        mBufferedReader = new BufferedReader(new InputStreamReader(mChannel.getInputStream()));
        mDataOutputStream = new DataOutputStream(mChannel.getOutputStream());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String line;
                    while (true) {
                        while ((line = mBufferedReader.readLine()) != null) {
                            final String result = line;
                            if (mSshText == null) mSshText = result;
                            myHandler.post(new Runnable() {
                                public void run() {
                                    synchronized (myEditText) {
                                        ((SshEditText)myEditText).setPrompt(result); //set the prompt to be the current line, so eventually it will be the last line.
                                        myEditText.setText(myEditText.getText().toString() + "\r\n" + result + "\r\n"+fetchPrompt(result));
                                        Log.d(TAG, "LINE : " + result);
                                    }
                                }
                            });
                        }

                    }
                } catch (Exception e) {
                    Log.e(TAG, " Exception " + e.getMessage() + "." + e.getCause() + "," + e.getClass().toString());
                }
            }
        }).start();
    }

    /**
     * Gets the prompt text from the returned string.
     * @param returnedString
     * @return
     */
    public static String fetchPrompt(String returnedString){
        return "";
    }

    /**
     * Removes the prompt string from the user input command.
     * @param command
     * @return
     */
    public static String removePrompt(String command){
        if(command != null && command.trim().split("\\$").length > 1){
            String[] split = command.trim().split("\\$");
            String s = "";
            for(int i = 1; i< split.length; i++){
                s += split[i];
            }
            return s;
        }
        return command;
    }
}


