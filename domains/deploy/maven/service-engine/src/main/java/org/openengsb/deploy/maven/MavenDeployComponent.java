package org.openengsb.deploy.maven;

import java.util.List;

import org.apache.servicemix.common.DefaultComponent;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;

/**
 * @org.apache.xbean.XBean element="mavenDeployComponent"
 *                         description="Deploy maven connector component"
 */
public class MavenDeployComponent extends DefaultComponent {
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
        return new Class[] { MavenDeployEndpoint.class };
    }
}
