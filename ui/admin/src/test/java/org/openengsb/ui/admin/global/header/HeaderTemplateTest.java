/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.admin.global.header;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.injection.annot.test.AnnotApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.remote.ProxyFactory;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.services.internal.DefaultWiringService;
import org.openengsb.core.test.NullEvent;
import org.openengsb.domain.auditing.AuditingDomain;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.global.footer.imprintPage.ImprintPage;
import org.openengsb.ui.admin.index.Index;
import org.openengsb.ui.admin.model.OpenEngSBVersion;
import org.openengsb.ui.admin.sendEventPage.SendEventPage;
import org.openengsb.ui.admin.testClient.TestClient;
import org.osgi.framework.BundleContext;

public class HeaderTemplateTest extends AbstractUITest {

    @Before
    public void setup() {

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
    }

    private boolean testNavigation(Class<? extends WebPage> page, String expectedIndexName) {
        tester.startPage(page);
        return HeaderTemplate.getActiveIndex().equals(expectedIndexName);
    }

    private void setupTestClientPage() {
        context.putBean(bundleContext);
        context.putBean(mock(ProxyFactory.class));
        setupTesterWithSpringMockContext();
    }

    private void setupIndexPage() {
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
        context.putBean("contextCurrentService", mock(ContextCurrentService.class));
        context.putBean("ruleManagerBean", mock(RuleManager.class));
        context.putBean("openengsbVersion", new OpenEngSBVersion());
        context.putBean("auditing", mock(AuditingDomain.class));
        context.putBean("wiringService", new DefaultWiringService());
        context.putBean(mock(ProxyFactory.class));
        BundleContext bundleContext = mock(BundleContext.class);
        context.putBean(bundleContext);
        List<Class<? extends Event>> eventClasses = Arrays.<Class<? extends Event>> asList(NullEvent.class);
        tester.startPage(new SendEventPage(eventClasses));
        tester.startPage(Index.class);
    }

}
