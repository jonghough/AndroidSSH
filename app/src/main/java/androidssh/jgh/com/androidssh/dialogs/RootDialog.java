package androidssh.jgh.com.androidssh.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;


/**
 * Created by jon on 5/21/14.
 */
 public class RootDialog {

        /**
         * calling activity
         */
        protected Activity mActivity;

        /**
         * the alertdialog builder
         */
        protected AlertDialog.Builder mBuilder;

        protected final AlertDialog mDialog;

        protected View mLayout;

    /**
     *
     * @param activity
     * @param rootLayout
     */
        public RootDialog(Activity activity, int rootLayout) {
            mActivity = activity;
            LayoutInflater inflater = (LayoutInflater)mActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            mLayout = inflater.inflate(rootLayout, null);

            mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(mActivity, android.R.style.Theme_Dialog));
            mBuilder.setView(mLayout);
            mDialog = mBuilder.create();
        }


}