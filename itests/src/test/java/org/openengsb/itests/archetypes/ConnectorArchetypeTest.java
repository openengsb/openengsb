package org.openengsb.itests.archetypes;

import java.io.File;
import java.io.FileWriter;
import java.util.Properties;
import java.util.Scanner;

public class ConnectorArchetypeTest extends AbstractArchetypeTest {
    @Override
    protected void addArchetypeData(Properties properties) {
        properties.put("archetypeGroupId", "org.openengsb.tooling.archetypes");
        properties.put("archetypeArtifactId", "org.openengsb.tooling.archetypes.connector");
        properties.put("archetypeVersion", "3.0.0-SNAPSHOT");
        properties.put("groupId", "org.openengsb.connector");
        properties.put("artifactId", "org.openengsb.connector.testconnector");
        properties.put("version", "3.0.0-SNAPSHOT");
        properties.put("name", "TestConnector");
        properties.put("package", "org.openengsb.connector.testconnector");
        properties.put("connectorName", "Testconnector");
        properties.put("connectorNameL", "testconnector");
        properties.put("domainInterface", "ExampleDomain");
        properties.put("domainL", "example");
        properties.put("domainVersion", "3.0.0-SNAPSHOT");
        properties.put("domainRange", "[3.0.0-SNAPSHOT)");
    }

    @Override
    protected void applyProjectModifications() throws Exception {
        // opens service implementation class for modification to avoid compilation errors
        File implFile = new File(
            TEST_ROOT + "/" 
            + systemProperties.getProperty("artifactId", DEFAULT_TEST_ARTIFACT_ID) 
            + "/src/main/java/org/openengsb/connector/testconnector/internal/TestconnectorServiceImpl.java"
            );
        Scanner scanner = new Scanner(implFile);
        String content = scanner.useDelimiter("\\Z").next();
        
        scanner.close();
        
        // add required imports
        content = content.replace(
            "import org.openengsb.domain.example.ExampleDomain;",
            "import org.openengsb.domain.example.ExampleDomain;\n" +
            "import org.openengsb.domain.example.event.LogEvent;\n" +
            "import org.openengsb.domain.example.model.ExampleRequestModel;\n" + 
            "import org.openengsb.domain.example.model.ExampleResponseModel;\n"
            );
        
        // add required method implementations
        content = content.replace(
            "// TODO implement domain methods here",
            "@Override\n" + 
            "public String doSomethingWithMessage(String message) { return null; }\n" +
            "@Override\n" +
            "public String doSomethingWithLogEvent(LogEvent event) { return null; }\n" +
            "@Override\n" + 
            "public ExampleResponseModel doSomethingWithModel(ExampleRequestModel model) { return null; }\n"
            );
        
        FileWriter writer = new FileWriter(implFile);
        
        writer.write(content);
        writer.close();
    }

}
