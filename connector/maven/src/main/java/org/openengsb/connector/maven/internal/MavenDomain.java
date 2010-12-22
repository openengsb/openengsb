package org.openengsb.connector.maven.internal;

import org.openengsb.domain.build.BuildDomain;
import org.openengsb.domain.deploy.DeployDomain;
import org.openengsb.domain.test.TestDomain;

public interface MavenDomain extends TestDomain, BuildDomain, DeployDomain {

}
