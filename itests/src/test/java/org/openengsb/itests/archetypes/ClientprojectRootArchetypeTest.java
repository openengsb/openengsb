package org.openengsb.itests.archetypes;

import java.util.Properties;

public class ClientprojectRootArchetypeTest extends AbstractArchetypeTest {

    @Override
    protected void addArchetypeData(Properties properties) {
        properties.put("archetypeGroupId", "org.openengsb.tooling.archetypes.clientproject");
        properties.put("archetypeArtifactId", "org.openengsb.tooling.archetypes.clientproject.root");
        properties.put("archetypeVersion", "3.0.0-SNAPSHOT");
    }

    @Override
    protected void applyProjectModifications() throws Exception {
        // TODO Auto-generated method stub

    }

}
