package org.openengsb.deploy.maven;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.drools.DeployDomain;
import org.openengsb.maven.common.AbstractMavenDomainImpl;

public class MavenDeployDomainImpl extends AbstractMavenDomainImpl implements DeployDomain {

    public MavenDeployDomainImpl(ContextHelper contextHelper) {
        super(contextHelper);
    }

    @Override
    public boolean deployProject() {
        return callMaven("deploy/maven-deploy").isSuccess();
    }

}
