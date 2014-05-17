package com.jgh.androidssh;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.jgh.androidssh.sshutils.SessionController;

/**
 * Created by jon on 5/17/14.
 */
public class TerminalActivity extends Activity{

    private TextView mTitleTextView, mTerminal;
    private SessionController mSessionController;
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        // Set no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.layout_terminal);

        mTerminal = (TextView)this.findViewById(R.id.terminaltextview);

    }
}
