package com.jgh.androidssh.sshutils;

import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * http://stackoverflow.com/questions/16298279/never-ending-of-reading-server-response-using-jsch
 * http://stackoverflow.com/questions/8949780/textview-act-like-a-terminal
 * Created by Jon Hough on 7/19/14.
 */
public class Console {

    private  EditText mEditText;

    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private static final String TAG = "CONSOLE";
    private SessionController mSessionController;

    public Console( EditText editText, SessionController sessionController){
           mEditText = editText;
        mSessionController = sessionController;

    }

    public void setInputStream(InputStream in){
        mInputStream = in;
    }

    public void setOutputStream(OutputStream out){
        mOutputStream = out;
    }


    public class SshInputStream extends InputStream{

        public SshInputStream(EditText editText){
            final EditText fedit = editText;
            editText.setOnKeyListener(new View.OnKeyListener() {


                public boolean onKey(View view, int key, KeyEvent event){
                    if(fedit != null && fedit.getText() != null){
                       String command = fedit.getText().toString();
                       if(command != null && command != ""){
                           try{
                            SshInputStream.this.read(command.getBytes());
                           }
                           catch( IOException e){
                               Log.v(TAG, "Exception, input stream reading string bytes "+e.getMessage());
                           }
                       }
                    }
                    return false;
                }

            });
        }


        @Override
        public int read(){

            return 0;
        }


        /**
         * http://stackoverflow.com/questions/23662414/client-socket-keep-socket-open-indefinitely
         */
       /*  private void setupOutputStream(){
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
             byte[] buffer = new byte[1024];

             int bytesRead;
             InputStream inputStream = mSession.getChannel().getInputStream();

             new Thread(new NetworkConnectHandler()).start();

             while (socket.isConnected())
             {
                 while ((bytesRead = inputStream.read(buffer)) != -1)
                 {
                     mReceivedPacket = null;
                     byteArrayOutputStream.write(buffer, 0, bytesRead);
                     mReceivedPacket = byteArrayOutputStream.toString("UTF-8");


                     if (mReceivedPacket.indexOf(STX) > -1 && mReceivedPacket.indexOf(ETX) > -1)
                     {
                         break;
                     }
                 }

                 byteArrayOutputStream.reset();

                 if (mReceivedPacket != null)
                 {
                     IncomingPacketHandler(XmlFromString(CleanIncomingPacket(mReceivedPacket)));
                 }
             }
         }
        catch (Exception ex)
        {
            Log.e("Network Connect", "S: Error", ex);
        }
         }*/


    }
}
