package org.openengsb.test.maven;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.drools.TestDomain;
import org.openengsb.maven.common.AbstractMavenDomainImpl;

public class MavenTestDomainImpl extends AbstractMavenDomainImpl implements TestDomain {

    public MavenTestDomainImpl(ContextHelper contextHelper) {
        super(contextHelper);
    }

    @Override
    public boolean runTests() {
        return callMaven("test/maven-test").isSuccess();
    }

}