package org.openengsb.deploy.maven;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.drools.DeployDomain;
import org.openengsb.drools.model.MavenResult;
import org.openengsb.maven.common.AbstractMavenDomainImpl;

public class MavenDeployDomainImpl extends AbstractMavenDomainImpl implements DeployDomain {

    public MavenDeployDomainImpl(ContextHelper contextHelper) {
        super(contextHelper);
    }

    @Override
    public MavenResult deployProject() {
        return callMaven("deploy/maven-deploy");
    }

}
