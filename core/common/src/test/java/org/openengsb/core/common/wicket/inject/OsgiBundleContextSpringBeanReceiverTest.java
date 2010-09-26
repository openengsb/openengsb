package org.openengsb.core.common.wicket.inject;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationContext;

public class OsgiBundleContextSpringBeanReceiverTest {

    @Test
    public void getBeanFromBundleWithOnePossibleBundle() throws Exception {
        Object mockbean = new Object();
        BundleContext bundleContext = setupBundleContextMock(mockbean, new ArrayList<ServiceReference>());

        OsgiBundleContextSpringBeanReceiver receiver = new OsgiBundleContextSpringBeanReceiver(bundleContext);
        Object actualBean = receiver.getBean("bean", "sym");

        Assert.assertEquals(mockbean, actualBean);
    }

    @Test
    public void getBeanFromBundleWithThreePossibleBundle() throws Exception {
        Object mockbean = new Object();
        List<ServiceReference> references = new ArrayList<ServiceReference>();
        createReference(references, "otherBundle");
        createReference(references, "otherBundle2");
        BundleContext bundleContext = setupBundleContextMock(mockbean, references);

        OsgiBundleContextSpringBeanReceiver receiver = new OsgiBundleContextSpringBeanReceiver(bundleContext);
        Object actualBean = receiver.getBean("bean", "sym");

        Assert.assertEquals(mockbean, actualBean);
    }

    private BundleContext setupBundleContextMock(Object mockbean, List<ServiceReference> references) throws Exception {
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        ServiceReference ref = createReference(references, "sym");
        ApplicationContext appContext = Mockito.mock(ApplicationContext.class);
        Mockito.when(appContext.getBean("bean")).thenReturn(mockbean);
        Mockito.when(bundleContext.getService(ref)).thenReturn(appContext);
        Mockito.when(bundleContext.getAllServiceReferences("org.springframework.context.ApplicationContext", null))
                .thenReturn(references.toArray(new ServiceReference[] {}));
        return bundleContext;
    }

    private ServiceReference createReference(List<ServiceReference> references, String symbolicName) {
        ServiceReference ref = Mockito.mock(ServiceReference.class);
        Mockito.when(ref.getProperty("org.springframework.context.service.name")).thenReturn(symbolicName);
        references.add(ref);
        return ref;
    }
}
