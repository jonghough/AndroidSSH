package com.jgh.androidssh.sshutils;

import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

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
    private BufferedReader mBufferedReader;
    private DataOutputStream mDataOutputStream;
    private Channel mChannel;
    private String mSshText = null;


    public ShellController() {
        //nothing
    }


    /**
     * Gets the dataoutputstream
     *
     * @return
     */
    public DataOutputStream getDataOutputStream() {
        return mDataOutputStream;
    }


    /**
     * Disconnects shell and closes streams.
     *
     * @throws IOException
     */
    public synchronized void disconnect() throws IOException {

        //close streams
        mDataOutputStream.flush();
        mDataOutputStream.close();
        mBufferedReader.close();
        //disconnect channel
        if (mChannel != null)
            mChannel.disconnect();
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
     * @throws IOException
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
                                        myEditText.setText(myEditText.getText().toString() + "\r\n" + result + "\r\n");
                                        Log.v(TAG, "LINE : " + result);
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

}


