package com.jgh.androidssh;

import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by Jon Hough on 7/29/14.
 */
public class SshEditText extends EditText {

    private String mlastInput;

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
    protected void onSelectionChanged(int s, int e) {
        //force selection to end
        setSelection(this.length());
    }

    public String getLastInput() {
        synchronized (this) {
            String rez = mlastInput;
            mlastInput = null;
            return rez;
        }
    }

    public String peekLastInput() {
        synchronized (this) {
            return mlastInput;
        }
    }

    public void AddLastInput(String s) {
        synchronized (this) {
            if (mlastInput == null) {
                mlastInput = "";
            }
            mlastInput = s;
        }
    }

    public int getCurrentCursorLine() {
        int selectionStart = Selection.getSelectionStart(this.getText());
        Layout layout = this.getLayout();

        if (!(selectionStart == -1)) {
            return layout.getLineForOffset(selectionStart);
        }

        return -1;
    }

    public boolean isNewLine() {
        int i = this.getText().toString().toCharArray().length;
        char s = this.getText().toString().toCharArray()[i - 1];
        if (s == '\n' || s == '\r') return true;

        return false;
    }
}
