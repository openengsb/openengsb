package org.openengsb.itests.archetypes;

import java.util.Properties;

public class DomainArchetypeTest extends AbstractArchetypeTest {

    @Override
    protected void addArchetypeData(Properties properties) {
        properties.put("archetypeGroupId", "org.openengsb.tooling.archetypes");
        properties.put("archetypeArtifactId", "org.openengsb.tooling.archetypes.domain");
        properties.put("archetypeVersion", "3.0.0-SNAPSHOT");
        properties.put("groupId", "org.openengsb.domain");
        properties.put("artifactId", "org.openengsb.domain.testdomain");
        properties.put("version", "3.0.0-SNAPSHOT");
        properties.put("name", "OpenEngSB :: Domain :: TestDomain");
        properties.put("package", "org.openengsb.domain.testdomain");
        properties.put("domainInterface", "TestDomain");
        properties.put("domainName", "testdomain");
        properties.put("openengsbVersion", "3.0.0-SNAPSHOT");
    }

    @Override
    protected void applyProjectModifications() throws Exception {
        // TODO Auto-generated method stub

    }

}
