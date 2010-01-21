package org.openengsb.build.maven;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.drools.BuildDomain;
import org.openengsb.drools.model.MavenResult;
import org.openengsb.maven.common.AbstractMavenDomainImpl;

public class MavenBuildDomainImpl extends AbstractMavenDomainImpl implements BuildDomain {

    public MavenBuildDomainImpl(ContextHelper contextHelper) {
        super(contextHelper);
    }

    @Override
    public MavenResult buildProject() {
        return callMaven("build/maven-build");
    }
}
