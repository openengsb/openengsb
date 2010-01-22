package org.openengsb.maven.common;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.openengsb.contextcommon.ContextHelper;

public class AbstractMavenDomainImpl {

    private ContextHelper contextHelper;

    public AbstractMavenDomainImpl(ContextHelper contextHelper) {
        this.contextHelper = contextHelper;
    }

    protected MavenResult callMaven(String pathPrefix) {
        Properties executionRequestProperties = getPropertiesFromContext(pathPrefix
                + "/config/executionRequestProperties");

        String baseDir = contextHelper.getValue(pathPrefix + "/config/baseDirectory");
        File baseDirectory = new File(baseDir);

        String[] goals = getValuesFromContext(pathPrefix + "/config/goals");

        MavenConnector maven = new MavenConnector(baseDirectory, goals, executionRequestProperties);
        return maven.execute();
    }

    protected Properties getPropertiesFromContext(String path) {
        Map<String, String> props = contextHelper.getAllValues(path);

        Properties properties = new Properties();
        for (Entry<String, String> entry : props.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return properties;
    }

    protected String[] getValuesFromContext(String path) {
        String values = contextHelper.getValue(path);
        return values.split(",");
    }

}
