package org.openengsb.test.maven;

import java.util.List;

import org.apache.servicemix.common.DefaultComponent;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;

/**
 * @org.apache.xbean.XBean element="mavenTestComponent"
 *                         description="Test maven connector Component"
 */
public class MavenTestComponent extends DefaultComponent {
    private OpenEngSBEndpoint[] endpoints;

    public OpenEngSBEndpoint[] getEndpoints() {
        return this.endpoints;
    }

    public void setEndpoints(OpenEngSBEndpoint[] endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    protected List<?> getConfiguredEndpoints() {
        return asList(this.endpoints);
    }

    @Override
    protected Class<?>[] getEndpointClasses() {
        return new Class[] { MavenTestEndpoint.class };
    }
}
