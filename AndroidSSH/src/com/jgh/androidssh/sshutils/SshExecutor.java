package com.jgh.androidssh.sshutils;

import java.io.IOException;

import com.jcraft.jsch.JSchException;

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
     * @throws IOException
     */
    public int executeCommand() throws JSchException, IOException;
    
    /**
     * Gets a return string.
     * @return
     */
    public String getString();
}
