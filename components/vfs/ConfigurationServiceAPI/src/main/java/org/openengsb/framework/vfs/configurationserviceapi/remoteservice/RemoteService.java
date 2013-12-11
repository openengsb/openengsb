package org.openengsb.framework.vfs.configurationserviceapi.remoteservice;

/**
 * Remote Services are services that need to be stopped while openengsb is being configured. That can for example be an http service that needs to be stopped to prevent access to openengsb during the reconfiguration. Every service that needs to be stopped during configuration needs to implement this interface.
 */
public interface RemoteService {
    boolean start();
    boolean stop();
}
