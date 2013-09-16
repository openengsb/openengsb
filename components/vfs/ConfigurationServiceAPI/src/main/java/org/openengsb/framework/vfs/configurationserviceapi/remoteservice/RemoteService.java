/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openengsb.framework.vfs.configurationserviceapi.remoteservice;

/**
 *
 * @author Richard
 */
public interface RemoteService {
    /**
     * Starts the remote service.
     * @return if the starting was successful.
     */
    boolean start();
    
    /**
     * Stops the remote service.
     * @return if the stopping was successful.
     */
    boolean stop();
}
