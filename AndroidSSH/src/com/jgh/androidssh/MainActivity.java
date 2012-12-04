package com.jgh.androidssh;

import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jgh.androidssh.channels.CommandExec;
import com.jgh.androidssh.channels.SessionUserInfo;
import com.jgh.androidssh.channels.SshExecutor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
    
    private TextView mTextView;
    private EditText mUserEdit;
    private EditText mHostEdit;
    private EditText mPasswordEdit;
    private EditText mCommandEdit;
    private Button mButton, mRunButton;
    private SessionUserInfo mSUI;
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        
        // Set no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.activity_main);
        
        mUserEdit = (EditText)findViewById(R.id.username);
        mHostEdit = (EditText)findViewById(R.id.hostname);
        mPasswordEdit = (EditText)findViewById(R.id.password);
        mButton = (Button)findViewById(R.id.enterbutton);
        mRunButton = (Button)findViewById(R.id.runbutton);
        mTextView = (TextView)findViewById(R.id.sshtext);
        mCommandEdit = (EditText)findViewById(R.id.command);
        
        // set onclicklistener
        mButton.setOnClickListener(this);
        mRunButton.setOnClickListener(this);
        
    }
    
    /**
     * AsyncTask to run the command.
     * 
     * @author Jonathan Hough
     * @since Dec 4 2012
     */
    public class SshTask extends AsyncTask<Void, Void, Boolean> {
        
        private SshExecutor mEx;
        
        private ProgressDialog mProgressDialog;
        
        //
        // Constructor
        //
        
        public SshTask(Context context, SshExecutor exec) {
            mEx = exec;
            mProgressDialog = new ProgressDialog(context);
            
        }
        
        @Override
        protected void onPreExecute() {
            mProgressDialog.setTitle(getResources().getText(R.string.progress_runningcommand));
            mProgressDialog.setMessage(getResources().getText(R.string.progress_pleasewait));
            mProgressDialog.show();
            
        }
        
        @Override
        protected Boolean doInBackground(Void... arg0) {
            boolean success = false;
            try {
                mEx.executeCommand();
            } catch (JSchException e) {
                
                makeToast(R.string.taskfail);
            } catch (IOException e) {
                makeToast(R.string.taskfail);
            }
            success = true;
            return success;
        }
        
        @Override
        protected void onPostExecute(Boolean b) {
            if (b) {
                mTextView.setText(mEx.getString());
            }
            mProgressDialog.dismiss();
            
        }
    }
    
    /**
     * Displays toast to user.
     * 
     * @param text
     */
    private void makeToast(int text) {
        Toast.makeText(this, getResources().getString(text), Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Checks if the EditText is empty.
     * 
     * @param editText
     * @return true if empty
     */
    private boolean isEditTextEmpty(EditText editText) {
        if (editText.getText().toString() == null || editText.getText().toString() == "") { return true; }
        return false;
    }
    
    public void onClick(View v) {
        if (v == mButton) {
            if (isEditTextEmpty(mUserEdit) || isEditTextEmpty(mHostEdit) || isEditTextEmpty(mPasswordEdit)) { return; }
            mSUI = new SessionUserInfo(mUserEdit.getText().toString(), mHostEdit.getText().toString(), mPasswordEdit
                            .getText().toString());
            
        }
        
        else if (v == mRunButton) {
            // check valid data
            if (isEditTextEmpty(mCommandEdit)) { return; }
            
            if (mSUI == null) {
                return;
            }
            
            // run command
            else {
                CommandExec com = new CommandExec(mCommandEdit.getText().toString().trim(), mSUI);
                
                new SshTask(this,com).execute();
            }
            
        }
        
    }
    
}
