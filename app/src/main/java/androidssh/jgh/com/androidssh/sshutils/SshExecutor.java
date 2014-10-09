package androidssh.jgh.com.androidssh.sshutils;

import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
/**
 * Interface for any objects which make JSch connections to implement.
 * @author Jonathan Hough
 *
 */
public interface SshExecutor {
    
    /**
     * Executes the command.
     * @return
     * @throws JSchException
     * @throws java.io.IOException
     */
    public int executeCommand(Session session) throws JSchException, IOException;
    
    /**
     * Gets a return string.
     * @return
     */
    public String getString();
}
