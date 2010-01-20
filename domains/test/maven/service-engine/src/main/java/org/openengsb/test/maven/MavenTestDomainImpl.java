package org.openengsb.test.maven;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.drools.TestDomain;
import org.openengsb.drools.model.MavenResult;
import org.openengsb.maven.common.MavenConnector;

public class MavenTestDomainImpl implements TestDomain {

    private ContextHelper contextHelper;

    public MavenTestDomainImpl(ContextHelper contextHelper) {
        this.contextHelper = contextHelper;
    }

    @Override
    public MavenResult runTests() {
        Properties executionRequestProperties = getPropertiesFromContext("test/maven-test/config/executionRequestProperties");

        String baseDir = contextHelper.getValue("test/maven-test/config/baseDirectory");
        File baseDirectory = new File(baseDir);

        String[] goals = getValuesFromContext("test/maven-test/config/goals");

        MavenConnector maven = new MavenConnector(baseDirectory, goals, executionRequestProperties);
        return maven.execute();
    }

    private Properties getPropertiesFromContext(String path) {
        Map<String, String> props = contextHelper.getAllValues(path);

        Properties properties = new Properties();
        for (Entry<String, String> entry : props.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return properties;
    }

    private String[] getValuesFromContext(String path) {
        String values = contextHelper.getValue(path);
        return values.split(",");
    }

}
