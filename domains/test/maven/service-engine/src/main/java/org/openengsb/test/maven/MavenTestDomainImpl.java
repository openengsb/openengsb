package org.openengsb.test.maven;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.drools.TestDomain;
import org.openengsb.drools.model.MavenResult;
import org.openengsb.maven.common.AbstractMavenDomainImpl;

public class MavenTestDomainImpl extends AbstractMavenDomainImpl implements TestDomain {

    public MavenTestDomainImpl(ContextHelper contextHelper) {
        super(contextHelper);
    }

    @Override
    public MavenResult runTests() {
        return callMaven("test/maven-test");
    }

}