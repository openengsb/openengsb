package org.openengsb.framework.vfs.vfstestservices;

import org.openengsb.framework.vfs.configurationserviceapi.remoteservice.RemoteService;

public class TestRemoteService implements RemoteService {
    private boolean startSuccess;
    private boolean stopSuccess;
    
    public TestRemoteService(boolean startSuccess, boolean stopSuccess)
    {
        this.startSuccess = startSuccess;
        this.stopSuccess = stopSuccess;
    }
    
    @Override
    public boolean start() {
        return startSuccess;
    }

    @Override
    public boolean stop() {
        return stopSuccess;
    }
}
