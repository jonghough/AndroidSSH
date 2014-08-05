
package com.jgh.androidssh;

import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jgh.androidssh.sshutils.ConnectionStatusListener;
import com.jgh.androidssh.sshutils.ExecTaskCallbackHandler;
import com.jgh.androidssh.sshutils.SessionUserInfo;
import com.jgh.androidssh.sshutils.SshExecutor;
import com.jgh.androidssh.sshutils.SessionController;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main activity. Connect to SSH server and launch command shell.
 *
 * @author Jon Hough
 */
public class MainActivity extends Activity implements OnClickListener {

    private static final String TAG = "MainActivity";
    private TextView mTextView, mConnectStatus;
    private EditText mUserEdit;
    private EditText mHostEdit;
    private EditText mPasswordEdit;
    private SshEditText mCommandEdit;
    private Button mButton, mEndSessionBtn, mSftpButton, mRunButton;
    private SessionUserInfo mSUI;
    private SessionController mSessionController;

    private Handler mHandler;
    private Handler mTvHandler;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Set no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        mUserEdit = (EditText) findViewById(R.id.username);
        mHostEdit = (EditText) findViewById(R.id.hostname);
        mPasswordEdit = (EditText) findViewById(R.id.password);
        mButton = (Button) findViewById(R.id.enterbutton);
        mEndSessionBtn = (Button) findViewById(R.id.endsessionbutton);
        mSftpButton = (Button) findViewById(R.id.sftpbutton);
        mRunButton = (Button) findViewById(R.id.runbutton);
        mTextView = (TextView) findViewById(R.id.sshtext);
        mCommandEdit = (SshEditText) findViewById(R.id.command);
        mConnectStatus = (TextView) findViewById(R.id.connectstatus);
        // set onclicklistener
        mButton.setOnClickListener(this);
        mEndSessionBtn.setOnClickListener(this);
        mRunButton.setOnClickListener(this);
        mSftpButton.setOnClickListener(this);

        mConnectStatus.setText("Connect Status: NOT CONNECTED");
        //handlers
        mHandler = new Handler();
        mTvHandler = new Handler();
        //text change listener, for getting the current input changes.
        mCommandEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mCommandEdit.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (mCommandEdit.getCurrentCursorLine() < mCommandEdit.getLineCount() - 1 || mCommandEdit.isNewLine()) {
                        mCommandEdit.setSelection(mCommandEdit.getText().length());
                        return true;
                    }
                    return false;
                } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (isEditTextEmpty(mCommandEdit)) {
                        return true;
                    }

                    if (mSUI == null) {
                        makeToast(R.string.insertallvalues);
                        return true;
                    }

                    // run command
                    else {
                        // get the last line of terminal
                        String command = getLastLine();
                        ExecTaskCallbackHandler t = new ExecTaskCallbackHandler() {
                            @Override
                            public void onFail() {
                                makeToast(R.string.taskfail);
                            }

                            @Override
                            public void onComplete(String completeString) {
                                mTextView.setText(mTextView.getText() + "\n" + completeString
                                        + mUserEdit.getText().toString().trim() + "\n");
                            }
                        };
                        mCommandEdit.AddLastInput(command);
                        mSessionController.executeCommand(mHandler, mCommandEdit, t, command);

                    }
                }

                return false;
            }
        });
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
                mEx.executeCommand(mSessionController.getSession());
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
            mProgressDialog.dismiss();

            if (b) {
                mTextView.setText(mTextView.getText() + "\n" + mEx.getString() + "\n"
                        + mUserEdit.getText().toString().trim() + "\n");

            } else {
                makeToast(R.string.taskfail);
            }

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
     * Start activity to do SFTP transfer. User will choose from list of files
     * to transfer.
     */
    private void startSftpActivity() {
        Intent intent = new Intent(this, FileListActivity.class);
        String[] info = {
                mSUI.getUser(), mSUI.getHost(), mSUI.getPassword()
        };

        intent.putExtra("UserInfo", info);

        startActivity(intent);
    }

    /**
     * @return
     */
    private String getLastLine() {// unused
        int index = mCommandEdit.getText().toString().lastIndexOf("\n");
        if (index == -1) {
            return mCommandEdit.getText().toString().trim();
        }

        String lastLine = mCommandEdit.getText().toString()
                .substring(index, mCommandEdit.getText().toString().length());

        return lastLine.trim();
    }

    /**
     * Checks if the EditText is empty.
     *
     * @param editText
     * @return true if empty
     */
    private boolean isEditTextEmpty(EditText editText) {
        if (editText.getText() == null || editText.getText().toString().equalsIgnoreCase("")) {
            return true;
        }
        return false;
    }

    public void onClick(View v) {
        if (v == mButton) {
            if (isEditTextEmpty(mUserEdit) || isEditTextEmpty(mHostEdit)
                    || isEditTextEmpty(mPasswordEdit)) {
                return;
            }
            mSUI = new SessionUserInfo(mUserEdit.getText().toString().trim(), mHostEdit.getText()
                    .toString().trim(),
                    mPasswordEdit.getText().toString().trim());
            mSessionController = SessionController.getSessionController();
            mSessionController.setUserInfo(mSUI);
            mSessionController.connect();

            mSessionController.setConnectionStatusListener(new ConnectionStatusListener() {
                @Override
                public void onDisconnected() {

                    mTvHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mConnectStatus.setText("Connection Status: NOT CONNECTED");
                        }
                    });
                }

                @Override
                public void onConnected() {
                    mTvHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mConnectStatus.setText("Connection Status: CONNECTED");
                        }
                    });
                }
            });

        } else if (v == mRunButton) {
            // check valid data
            if (isEditTextEmpty(mCommandEdit)) {
                return;
            }

            if (mSUI == null) {
                makeToast(R.string.insertallvalues);
                return;
            }

            // run command
            else {


                // get the last line of terminal
                String command = getLastLine();
                ExecTaskCallbackHandler t = new ExecTaskCallbackHandler() {
                    @Override
                    public void onFail() {
                        makeToast(R.string.taskfail);
                    }

                    @Override
                    public void onComplete(String completeString) {
                        mTextView.setText(mTextView.getText() + "\n" + completeString
                                + mUserEdit.getText().toString().trim() + "\n");
                    }
                };
                mCommandEdit.AddLastInput(command);
                mSessionController.executeCommand(mHandler, mCommandEdit, t, command);
                /*if (mSUI != null) {
                    if(mComEx == null)
                        mComEx = new CommandExec();
                       
                    mComEx.setCommand(command);
                        
                    new SshTask(this, mComEx).execute();
                } else {
                    makeToast(R.string.insertallvalues);
                    return;
                }*/

            }

        } else if (v == mSftpButton) {
            if (mSUI != null) {

                startSftpActivity();

            }
        } else if (v == this.mEndSessionBtn) {
            try {
                mSessionController.disconnect();
            } catch (IOException e) {
                Log.e(TAG, "Disconnect exception " + e.getMessage());
            }
        }

    }


}
