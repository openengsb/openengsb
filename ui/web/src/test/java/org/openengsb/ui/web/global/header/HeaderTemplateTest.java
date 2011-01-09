/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.web.global.header;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.injection.annot.test.AnnotApplicationContextMock;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.proxy.ProxyFactory;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.core.test.NullEvent;
import org.openengsb.domain.auditing.AuditingDomain;
import org.openengsb.ui.web.Index;
import org.openengsb.ui.web.SendEventPage;
import org.openengsb.ui.web.TestClient;
import org.openengsb.ui.web.global.footer.ImprintPage;
import org.openengsb.ui.web.model.OpenEngSBVersion;
import org.osgi.framework.BundleContext;

public class HeaderTemplateTest {

    private WicketTester tester;
    private ApplicationContextMock context;

    @Before
    public void setup() {
        tester = new WicketTester();
        context = new ApplicationContextMock();
        context.putBean(Mockito.mock(ContextCurrentService.class));
        context.putBean("openengsbVersion", new OpenEngSBVersion());
    }

    @Test
    public void testNavigationFieldForIndex() {
        setupIndexPage();
        Assert.assertTrue(testNavigation(Index.class, Index.class.getSimpleName()));
        Assert.assertEquals(Index.class, tester.getLastRenderedPage().getClass());
    }

    @Test
    public void testNavigationFieldForTestClient() {
        setupTestClientPage();
        Assert.assertTrue(testNavigation(TestClient.class, TestClient.class.getSimpleName()));
        Assert.assertEquals(TestClient.class, tester.getLastRenderedPage().getClass());
    }

    @Test
    public void testNavigationForNonExistingNavigationButton() {
        Assert.assertTrue(testNavigation(ImprintPage.class, Index.class.getSimpleName()));
        Assert.assertEquals(ImprintPage.class, tester.getLastRenderedPage().getClass());
    }

    @Test
    public void testToNavigate() {
        setUpSendEventPage();
        Assert.assertEquals(Index.class, tester.getLastRenderedPage().getClass());
        tester.clickLink("header:headerMenuItems:0:link");
        tester.assertRenderedPage(Index.class);
        tester.clickLink("header:headerMenuItems:1:link");
        tester.assertRenderedPage(TestClient.class);
        tester.clickLink("header:headerMenuItems:2:link");
        tester.assertRenderedPage(SendEventPage.class);
    }

    private boolean testNavigation(Class<? extends WebPage> page, String expectedIndexName) {
        tester.startPage(page);
        return HeaderTemplate.getActiveIndex().equals(expectedIndexName);
    }

    private void setupTestClientPage() {
        DomainService domainServiceMock = Mockito.mock(DomainService.class);
        context.putBean(domainServiceMock);
        BundleContext bundleContext = mock(BundleContext.class);
        context.putBean(bundleContext);
        context.putBean(mock(ProxyFactory.class));
        setupTesterWithSpringMockContext();
    }

    private void setupIndexPage() {
        DomainService domainServiceMock = Mockito.mock(DomainService.class);
        context.putBean(domainServiceMock);
        setupTesterWithSpringMockContext();
        tester.startPage(Index.class);
    }

    private void setupTesterWithSpringMockContext() {
        tester.getApplication().addComponentInstantiationListener(
            new SpringComponentInjector(tester.getApplication(), context, true));
    }

    @SuppressWarnings("unchecked")
    private void setUpSendEventPage() {
        tester = new WicketTester();
        AnnotApplicationContextMock context = new AnnotApplicationContextMock();
        tester.getApplication().addComponentInstantiationListener(
            new SpringComponentInjector(tester.getApplication(), context, false));
        WorkflowService eventService = mock(WorkflowService.class);
        context.putBean("eventService", eventService);
        context.putBean("domainService", mock(DomainService.class));
        context.putBean("contextCurrentService", mock(ContextCurrentService.class));
        context.putBean("ruleManagerBean", mock(RuleManager.class));
        context.putBean("openengsbVersion", new OpenEngSBVersion());
        context.putBean("auditing", mock(AuditingDomain.class));
        context.putBean(mock(ProxyFactory.class));
        BundleContext bundleContext = mock(BundleContext.class);
        context.putBean(bundleContext);
        List<Class<? extends Event>> eventClasses = Arrays.<Class<? extends Event>> asList(NullEvent.class);
        tester.startPage(new SendEventPage(eventClasses));
        tester.startPage(Index.class);
    }
}
