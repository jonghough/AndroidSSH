package androidssh.jgh.com.androidssh.sshutils;

/**
 * Created by jon on 7/26/14.
 */
public interface ExecTaskCallbackHandler {

    public void onFail();

    public void onComplete(String completeString);
}
