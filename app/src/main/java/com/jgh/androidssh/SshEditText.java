package com.jgh.androidssh;

import android.content.Context;
import android.text.InputType;
import android.text.Layout;
import android.text.Selection;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

/**
 * EditText class for simulating an SSH shell terminal.
 * Main features of this class, compared to base EditText,
 * is the inability to delete (backspace) characters from
 * lines above the bottom line, as in a terminal.
 *
 * Created by Jon Hough on 7/29/14.
 */
public class SshEditText extends EditText {

    private String mlastInput;

    private String mPrompt = null;
    /**
     * First Constructor
     * @param context
     */
    public SshEditText(Context context) {
        super(context);
        setup();
    }

    /**
     * Second Constructor
     * @param context
     * @param attrs
     */
    public SshEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    /**
     * Third constructor
     * @param context
     * @param attrs
     * @param defStyle
     */
    public SshEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    public void setup(){
        this.setRawInputType(InputType.TYPE_CLASS_TEXT);
        this.setImeOptions(EditorInfo.IME_ACTION_GO);
    }

    @Override
    protected void onSelectionChanged(int s, int e) {
        //force selection to end
        setSelection(this.length());
    }

    /**
     * Returns the last line of user input.
     * @return string input
     */
    public String getLastInput() {
        synchronized (this) {
            String rez = mlastInput;
            mlastInput = null;
            return rez;
        }
    }

    /**
     * Peeks the last line of user input.
     * @return string input
     */
    public String peekLastInput() {
        synchronized (this) {
            return new String(mlastInput);
        }
    }


    /**
     * Sets the last input member variable to s.
     * @param s
     */
    public void AddLastInput(String s) {
        synchronized (this) {
            if (mlastInput == null) {
                mlastInput = "";
            }
            mlastInput = s;
        }
    }


    /**
     * Gets the current line the cursor is on.
     * @return Current line, -1 if none.
     */
    public int getCurrentCursorLine() {
        int selectionStart = Selection.getSelectionStart(this.getText());
        Layout layout = this.getLayout();

        if (!(selectionStart == -1)) {
            return layout.getLineForOffset(selectionStart);
        }

        return -1;
    }

    /**
     * Returns true if currently on a new line with no
     * input on the line.
     * @return True if new line, false otherwise.
     */
    public boolean isNewLine() {
        int i = this.getText().toString().toCharArray().length;
        char s = this.getText().toString().toCharArray()[i - 1];
        if (s == '\n' || s == '\r') return true;

        return false;
    }

    /**
     * Sets the prompt string. If parameter is null, then the prompt parameter
     * will also be null.
     * @param prompt
     */
    public void setPrompt(String prompt){
        mPrompt = prompt;
    }
}
