package org.openengsb.domains.notification.email.internal;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.config.Domain;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.openengsb.domains.notification.NotificationDomain;
import org.openengsb.domains.notification.email.EmailServiceManager;
import org.openengsb.domains.notification.email.internal.abstraction.JavaxMailAbstraction;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;

public class EmailServiceManagerTest {

    @Test
    public void testGetDescriptor() throws Exception {
        BundleContext bundleContextMock = mockBundleContextForServiceManager();
        EmailNotifierBuilder builder = Mockito.mock(EmailNotifierBuilder.class);
        EmailServiceManager manager = createEmailManager(bundleContextMock, builder);

        ServiceDescriptor descriptor = manager.getDescriptor(Locale.ENGLISH);

        Assert.assertEquals(EmailNotifier.class.getName(), descriptor.getId());
        Assert.assertEquals(NotificationDomain.class.getName(), descriptor.getServiceInterfaceId());
        Assert.assertEquals(6, descriptor.getAttributes().size());
        Assert.assertEquals(EmailNotifier.class, descriptor.getType());
    }

    @Test
    public void testAddNewOne() throws Exception {
        BundleContext bundleContextMock = mockBundleContextForServiceManager();
        HashMap<String, String> attributes = new HashMap<String, String>();
        EmailNotifierBuilder builder = Mockito.mock(EmailNotifierBuilder.class);
        EmailNotifier mock = Mockito.mock(EmailNotifier.class);
        Mockito.when(builder.createEmailNotifier("test", attributes)).thenReturn(mock);

        EmailServiceManager manager = createEmailManager(bundleContextMock, builder);
        manager.update("test", attributes);

        Hashtable<String, String> props = createVerificationHashmap();
        Mockito.verify(bundleContextMock).registerService(
                new String[] { EmailNotifier.class.getName(), NotificationDomain.class.getName(),
                        Domain.class.getName() }, mock, props);
    }

    @Test
    public void testUpdateExistingOne() throws Exception {
        BundleContext bundleContextMock = mockBundleContextForServiceManager();
        EmailNotifierBuilder builder = Mockito.mock(EmailNotifierBuilder.class);
        EmailNotifier mock = Mockito.mock(EmailNotifier.class);
        HashMap<String, String> attributes = new HashMap<String, String>();
        Mockito.when(builder.createEmailNotifier("test", attributes)).thenReturn(mock);

        EmailServiceManager manager = createEmailManager(bundleContextMock, builder);
        manager.update("test", attributes);
        HashMap<String, String> verificationAttributes = new HashMap<String, String>();
        manager.update("test", verificationAttributes);

        Hashtable<String, String> props = createVerificationHashmap();
        Mockito.verify(builder, Mockito.times(1)).updateEmailNotifier(mock, verificationAttributes);
        Mockito.verify(bundleContextMock, Mockito.times(1)).registerService(
                new String[] { EmailNotifier.class.getName(), NotificationDomain.class.getName(),
                        Domain.class.getName() }, mock, props);
    }

    @Test
    public void testDeleteService() throws Exception {
        BundleContext bundleContextMock = mockBundleContextForServiceManager();
        HashMap<String, String> attributes = new HashMap<String, String>();
        EmailNotifierBuilder builder = Mockito.mock(EmailNotifierBuilder.class);
        EmailNotifier mock = new EmailNotifier("id", new JavaxMailAbstraction());
        Mockito.when(builder.createEmailNotifier("test", attributes)).thenReturn(mock);
        ServiceRegistration serviceRegistrationMock = appendServiceRegistrationMockToBundleContextMock(
                bundleContextMock, mock);

        EmailServiceManager manager = createEmailManager(bundleContextMock, builder);
        manager.update("test", attributes);
        manager.delete("test");

        Mockito.verify(serviceRegistrationMock).unregister();
    }

    private ServiceRegistration appendServiceRegistrationMockToBundleContextMock(BundleContext bundleContextMock,
            EmailNotifier mock) {
        ServiceRegistration serviceRegistrationMock = Mockito.mock(ServiceRegistration.class);
        Hashtable<String, String> props = createVerificationHashmap();
        Mockito.when(
                bundleContextMock.registerService(new String[] { EmailNotifier.class.getName(),
                        NotificationDomain.class.getName(), Domain.class.getName() }, mock, props)).thenReturn(
                serviceRegistrationMock);
        return serviceRegistrationMock;
    }

    private Hashtable<String, String> createVerificationHashmap() {
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put("id", "test");
        props.put("domain", NotificationDomain.class.getName());
        props.put("class", EmailNotifier.class.getName());
        return props;
    }

    private EmailServiceManager createEmailManager(BundleContext bundleContextMock, EmailNotifierBuilder builder) {
        EmailServiceManager manager = new EmailServiceManager(builder);
        manager.setBundleContext(bundleContextMock);
        manager.init();
        return manager;
    }

    private BundleContext mockBundleContextForServiceManager() {
        Dictionary<String, String> dict = new Hashtable<String, String>();
        dict.put(Constants.BUNDLE_LOCALIZATION, ".");

        Bundle bundleMock = Mockito.mock(Bundle.class);
        Mockito.when(bundleMock.getHeaders()).thenReturn(dict);

        BundleContext bundleContextMock = Mockito.mock(BundleContext.class);
        Mockito.when(bundleContextMock.getBundle()).thenReturn(bundleMock);
        return bundleContextMock;
    }
}
