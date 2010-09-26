package org.openengsb.core.common.wicket.inject;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.wicket.inject.demopage.Page;
import org.openengsb.core.common.wicket.inject.demopage.PageService;

public class TestOsgiSpringComponentInjector {

    @Test
    public void testInjectMockedBean() throws Exception {
        WicketTester tester = new WicketTester();
        OsgiSpringBeanReceiver beanReceiver = Mockito.mock(OsgiSpringBeanReceiver.class);
        PageService testServiceMock = Mockito.mock(PageService.class);
        Mockito.when(testServiceMock.getHelloWorldText()).thenReturn("helloWorldText");
        Mockito.when(beanReceiver.getBean("testBean", "testName")).thenReturn(testServiceMock);
        tester.getApplication().addComponentInstantiationListener(
                new OsgiSpringComponentInjector(tester.getApplication(), beanReceiver));
        tester.startPage(Page.class);
        tester.assertContains("helloWorldText");
    }
}
