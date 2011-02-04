package org.openengsb.core.deployer.connector.internal;

import java.io.File;
import java.io.IOException;

public interface DeployerStorage {
    
    public void put(File file, ConnectorConfiguration config) throws IOException;
    public void remove(File file) throws IOException;
    public String getServiceId(File file) throws IOException;
    public String getConnectorType(File file) throws IOException;

}
