package com.jgh.androidssh;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by Jon Hough on 7/29/14.
 */
public class SshEditText extends EditText {

    public SshEditText(Context context) {
        super(context);

    }

    public SshEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public SshEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    @Override
    protected void onSelectionChanged(int s, int e){
        //force selection to end
        setSelection(this.length());
    }
}
