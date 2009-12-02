package org.openengsb.linking.http;

import java.util.List;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.Endpoint;

/**
 * @org.apache.xbean.XBean element="linkhttpComponent"
 *                         description="edb Component" The edb-jbi-component
 *
 */
public class LinkingHttpComponent extends DefaultComponent {
    private Endpoint[] endpoints;

    public Endpoint[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Endpoint[] endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    protected List<?> getConfiguredEndpoints() {
        return asList(endpoints);
    }

    @Override
    protected Class<?>[] getEndpointClasses() {
        return new Class[] { Endpoint.class };
    }
}
