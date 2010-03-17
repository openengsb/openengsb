package org.openengsb.trac.xmlrpc;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfig;

public class XmlRpcClientSpringHelper extends XmlRpcClient {
    public XmlRpcClientConfig getConfig() {
        return super.getClientConfig();
    }

    @Override
    public void setConfig(XmlRpcClientConfig pConfig) {
        super.setConfig(pConfig);
    }

}
