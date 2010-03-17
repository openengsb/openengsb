package org.openengsb.trac.xmlrpc;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfig;

/**
 * This class is required when using Spring 2.5 and Apache's XmlRpcClient. Due
 * to the fact that in the original class the methods getConfig and setConfig
 * have different parameter types, Spring won't allow to inject the
 * config-property. Therefore this class extends the original one, providing a
 * "correct" pair of get- and set-method.
 */
public class XmlRpcClientSpringHelper extends XmlRpcClient {
    public XmlRpcClientConfig getConfig() {
        return super.getClientConfig();
    }

    @Override
    public void setConfig(XmlRpcClientConfig pConfig) {
        super.setConfig(pConfig);
    }

}
