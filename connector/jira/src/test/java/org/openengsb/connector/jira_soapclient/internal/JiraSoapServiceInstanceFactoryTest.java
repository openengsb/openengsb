package org.openengsb.connector.jira_soapclient.internal;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.openengsb.connector.jira_soapclient.internal.JiraSoapServiceInstanceFactory;
import org.openengsb.connector.jira_soapclient.internal.SOAPClient;

public class JiraSoapServiceInstanceFactoryTest {
    
    @Test
    public void testCreatePlaintextReportService() throws Exception {
        JiraSoapServiceInstanceFactory factory = new JiraSoapServiceInstanceFactory();
        Map<String, String> attributes = new HashMap<String, String>();
        SOAPClient jiraService = factory.createServiceInstance("id", attributes);

        Assert.assertNotNull(jiraService);
    }
}
