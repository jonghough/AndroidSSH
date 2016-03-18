package com.jgh.androidssh.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jgh.androidssh.R;
import com.jgh.androidssh.sshutils.ConnectionStatusListener;
import com.jgh.androidssh.sshutils.SessionController;
import com.jgh.androidssh.sshutils.SessionUserInfo;


public class SshConnectFragmentDialog  extends DialogFragment implements View.OnClickListener {

    private EditText mUserEdit;
    private EditText mHostEdit;
    private EditText mPasswordEdit;
    private EditText mPortNumEdit;
    private Button mButton;


    private SessionUserInfo mSUI;
    private ConnectionStatusListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void setListener(ConnectionStatusListener listenr){
        mListener = listenr;
    }

    public static SshConnectFragmentDialog newInstance() {
        SshConnectFragmentDialog fragment = new SshConnectFragmentDialog();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mUserEdit = (EditText) v.findViewById(R.id.username);
        mHostEdit = (EditText) v.findViewById(R.id.hostname);
        mPasswordEdit = (EditText) v.findViewById(R.id.password);
        mPortNumEdit = (EditText) v.findViewById(R.id.portnum);
        mButton = (Button) v.findViewById(R.id.enterbutton);
        mButton.setOnClickListener(this);
        return v;
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

    @Override
    public void onClick(View v) {
        if (v == mButton) {
            if (isEditTextEmpty(mUserEdit) || isEditTextEmpty(mHostEdit)
                    || isEditTextEmpty(mPasswordEdit) || isEditTextEmpty(mPortNumEdit)) {
                return;
            }
            int port = Integer.valueOf(mPortNumEdit.getText().toString());
            mSUI = new SessionUserInfo(mUserEdit.getText().toString().trim(), mHostEdit.getText()
                    .toString().trim(),
                    mPasswordEdit.getText().toString().trim(), port);

            SessionController.getSessionController().setUserInfo(mSUI);
            SessionController.getSessionController().connect();

            if(mListener != null)
                SessionController.getSessionController().setConnectionStatusListener(mListener);


//
//
//                    new ConnectionStatusListener() {
//                @Override
//                public void onDisconnected() {
//
//
////                    mTvHandler.post(new Runnable() {
////                        @Override
////                        public void run() {
////                            mConnectStatus.setText("Connection Status: NOT CONNECTED");
////                        }
////                    });
//                }
//
//                @Override
//                public void onConnected() {
//
//
////                    mTvHandler.post(new Runnable() {
////                        @Override
////                        public void run() {
////                            mConnectStatus.setText("Connection Status: CONNECTED");
////                        }
////                    });
//                }
//            });
            this.dismiss();

        }
    }


}


