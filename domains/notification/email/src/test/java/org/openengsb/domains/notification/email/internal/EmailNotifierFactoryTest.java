package org.openengsb.domains.notification.email.internal;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.domains.notification.email.internal.abstraction.MailAbstraction;

public class EmailNotifierFactoryTest {

    @Test
    public void testCreateEmailNotifier() throws Exception {
        MailAbstraction mailAbstraction = Mockito.mock(MailAbstraction.class);
        EmailNotifierBuilder factory = new DefaultEmailNotifierBuilder(mailAbstraction);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("user", "user");
        attributes.put("password", "password");
        attributes.put("smtpAuth", "smtpAuth");
        attributes.put("smtpSender", "smtpSender");
        attributes.put("smtpHost", "smtpHost");
        attributes.put("smtpPort", "smtpPort");

        EmailNotifier notifier = factory.createEmailNotifier("id", attributes);

        Assert.assertNotNull(notifier);
        Assert.assertEquals("id", notifier.getId());
        Assert.assertEquals("user", notifier.getUser());
        Assert.assertEquals("password", notifier.getPassword());
        Assert.assertEquals("smtpAuth", notifier.getSmtpAuth());
        Assert.assertEquals("smtpSender", notifier.getSmtpSender());
        Assert.assertEquals("smtpHost", notifier.getSmtpHost());
        Assert.assertEquals("smtpPort", notifier.getSmtpPort());
    }

    @Test
    public void testUpdateEmailNotifier() throws Exception {
        MailAbstraction mailAbstraction = Mockito.mock(MailAbstraction.class);
        EmailNotifierBuilder factory = new DefaultEmailNotifierBuilder(mailAbstraction);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("user", "user");
        attributes.put("password", "password");
        attributes.put("smtpAuth", "smtpAuth");
        attributes.put("smtpSender", "smtpSender");
        attributes.put("smtpHost", "smtpHost");
        attributes.put("smtpPort", "smtpPort");

        EmailNotifier notifier = factory.createEmailNotifier("id", attributes);

        attributes.put("smtpSender", "otherValue");

        factory.updateEmailNotifier(notifier, attributes);

        Assert.assertEquals("otherValue", notifier.getSmtpSender());
    }
}
