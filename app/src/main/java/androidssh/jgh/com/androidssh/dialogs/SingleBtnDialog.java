package androidssh.jgh.com.androidssh.dialogs;

import android.app.Activity;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by jon on 5/24/14.
 */
public class SingleBtnDialog extends RootDialog {

    private Button mButton;
    private TextView mTextView;
    private ActionHandler mActionHandler;

    public SingleBtnDialog(Activity activity, int rootLayout, String text, ActionHandler actionHandler ) {
        super(activity, rootLayout);
        mTextView.setText(text);
        mActionHandler = actionHandler;
    }

}
