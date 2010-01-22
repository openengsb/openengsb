package org.openengsb.build.maven;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.drools.BuildDomain;
import org.openengsb.maven.common.AbstractMavenDomainImpl;

public class MavenBuildDomainImpl extends AbstractMavenDomainImpl implements BuildDomain {

    public MavenBuildDomainImpl(ContextHelper contextHelper) {
        super(contextHelper);
    }

    @Override
    public boolean buildProject() {
        return callMaven("build/maven-build").isSuccess();
    }
}
