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

package org.openengsb.ui.admin.global.menu;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.wicket.markup.html.WebPage;
import org.junit.Test;
import org.openengsb.core.api.Event;
import org.openengsb.core.test.NullEvent;
import org.openengsb.core.workflow.api.WorkflowService;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.global.footer.imprintPage.ImprintPage;
import org.openengsb.ui.admin.index.Index;
import org.openengsb.ui.admin.sendEventPage.SendEventPage;
import org.openengsb.ui.admin.testClient.TestClient;
import org.openengsb.ui.admin.userService.UserListPage;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;
import org.springframework.aop.framework.ProxyFactory;

public class MenuTemplateTest extends AbstractUITest {

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
        tester.clickLink("menu:menuItems:0:link");
        tester.debugComponentTrees();
        tester.assertRenderedPage(Index.class);
        tester.clickLink("menu:menuItems:1:link");
        tester.assertRenderedPage(UserListPage.class);
    }

    private boolean testNavigation(Class<? extends WebPage> page, String expectedIndexName) {
        tester.startPage(page);
        return MenuTemplate.getActiveIndex().equals(expectedIndexName);
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
        tester.getApplication().getComponentInstantiationListeners().add(
            new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));
    }

    @SuppressWarnings("unchecked")
    private void setUpSendEventPage() {
        WorkflowService eventService = mock(WorkflowService.class);
        context.putBean("eventService", eventService);
        List<Class<? extends Event>> eventClasses = Arrays.<Class<? extends Event>> asList(NullEvent.class);
        tester.startPage(new SendEventPage(eventClasses));
        tester.startPage(Index.class);
    }

}
