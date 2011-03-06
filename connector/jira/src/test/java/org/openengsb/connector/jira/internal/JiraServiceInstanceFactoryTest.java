package org.openengsb.connector.jira.internal;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class JiraServiceInstanceFactoryTest {
    
    @Test
    public void testCreatePlaintextReportService() throws Exception {
        JiraServiceInstanceFactory factory = new JiraServiceInstanceFactory();
        Map<String, String> attributes = new HashMap<String, String>();
        JiraService jiraService = factory.createServiceInstance("id", attributes);

        Assert.assertNotNull(jiraService);
    }
}
