package org.openengsb.framework.vfs.api.remoteservice;

import org.openengsb.framework.vfs.api.exceptions.RemoteServiceException;

/**
 * Remote Services are services that need to be stopped while openengsb is being configured. 
 * That can for example be an http service that needs to be stopped to prevent access to
 * openengsb during the reconfiguration. Every service that needs to be stopped during
 * configuration needs to implement this interface.
 */
public interface RemoteService {
    void start() throws RemoteServiceException;
    void stop() throws RemoteServiceException;
}
