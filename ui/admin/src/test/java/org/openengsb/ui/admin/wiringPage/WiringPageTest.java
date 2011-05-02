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

package org.openengsb.ui.admin.wiringPage;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.tester.FormTester;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.common.util.DictionaryAsMap;
import org.openengsb.ui.admin.AbstractUITest;

public class WiringPageTest extends AbstractUITest {

    public interface TestDomainInterface extends Domain {
    }
    public interface AnotherTestDomainInterface extends Domain {
    }
    
    private ConnectorId testdomainConnectorId;
    private final String globTest = "globTest";
    
    @Before
    public void setUp() throws Exception {
        RuleManager ruleManager = mock(RuleManager.class);
        Map<String, String> globals = new TreeMap<String, String>();
        globals.put(globTest, TestDomainInterface.class.getCanonicalName());
        globals.put("anotherGlob", AnotherTestDomainInterface.class.getCanonicalName());
        when(ruleManager.listGlobals()).thenReturn(globals);
        context.putBean(ruleManager);
        createConnectors();
        tester.getApplication()
            .addComponentInstantiationListener(new SpringComponentInjector(tester.getApplication(), context, true));
        tester.startPage(WiringPage.class);
    }
    
    @Test
    public void wiringPage_shouldBeRendered() {
        tester.assertRenderedPage(WiringPage.class);
        tester.assertComponent("domainChooseForm", Form.class);
        tester.assertComponent("domainChooseForm:domains", DropDownChoice.class);
        tester.assertComponent("globals", LinkTree.class);
        tester.assertComponent("endpoints", LinkTree.class);
        tester.assertComponent("wiringForm", Form.class);
        tester.assertComponent("wiringForm:globalName", TextField.class);
        tester.assertComponent("wiringForm:wireButton", AjaxSubmitLink.class);
        tester.assertDisabled("wiringForm:wireButton");
        tester.assertComponent("wiringForm:instanceId", TextField.class);
        tester.assertComponent("feedbackPanel", FeedbackPanel.class);
    }
    
    @Test
    public void domains_shouldBeLoaded() {
        @SuppressWarnings("unchecked")
        DropDownChoice<Class<? extends Domain>> domains = (DropDownChoice<Class<? extends Domain>>) 
            tester.getComponentFromLastRenderedPage("domainChooseForm:domains");
        assertThat(domains.getChoices().size(), is(2));
        assertThat(domains.getChoices().get(0), IsAssignableFrom.isAssignableFrom(AnotherTestDomainInterface.class));
        assertThat(domains.getChoices().get(1), IsAssignableFrom.isAssignableFrom(TestDomainInterface.class));
    }
    
    @Test
    public void selectDomain_shouldUpdateGlobals() {
        selectDomain(1); //TestDomainInterface
        LinkTree globals = (LinkTree) tester.getComponentFromLastRenderedPage("globals");
        TreeModel tree = globals.getModelObject();
        assertThat(tree.getChildCount(tree.getRoot()), is(1));
        assertThat(tree.getChild(tree.getRoot(), 0).toString(), is(globTest));
    }
    
    @Test
    public void selectDomain_shouldUpdateEndpoints() {
        selectDomain(1); //TestDomainInterface
        LinkTree endpoints = (LinkTree) tester.getComponentFromLastRenderedPage("endpoints");
        TreeModel tree = endpoints.getModelObject();
        assertThat(tree.getChildCount(tree.getRoot()), is(1));
        assertThat(tree.getChild(tree.getRoot(), 0).toString(), is(testdomainConnectorId.toFullID()));
    }
    
    @Test
    public void selectGlobal_shouldUpdateLabel() {
        selectDomain(1);
        selectFirstGlobal();
        TextField<?> epLabel = (TextField<?>) tester.getComponentFromLastRenderedPage("wiringForm:globalName");
        assertThat(epLabel.getDefaultModelObject().toString(), is(globTest));
    }
    
    @Test
    public void selectGlobalsRootNode_shouldNotUpdateLabel() {
        tester.clickLink("globals:i:0:nodeComponent:contentLink");
        TextField<?> epLabel = (TextField<?>) tester.getComponentFromLastRenderedPage("wiringForm:globalName");
        assertThat(epLabel.getDefaultModelObject().toString(), is(""));
    }
    
    @Test
    public void selectEndpoint_shouldUpdateLabel() {
        selectDomain(1);
        selectFirstEndpoint();
        TextField<?> epLabel = (TextField<?>) tester.getComponentFromLastRenderedPage("wiringForm:instanceId");
        assertThat(epLabel.getDefaultModelObject().toString(), is(testdomainConnectorId.toFullID()));
    }
    
    @Test
    public void selectGlobalAndEndpoint_shouldMakeWiringPossible() {
        selectDomain(1); //TestDomainInterface
        selectFirstGlobal();
        selectFirstEndpoint();
        tester.assertEnabled("wiringForm:wireButton");
    }

    @Test
    public void wire_NewLocationProperty_shouldUpdateServiceProperties() {
        selectDomain(1); //TestDomainInterface
        selectFirstGlobal();
        selectFirstEndpoint();
        ContextHolder.get().setCurrentContextId("bar");
        tester.clickLink("wiringForm:wireButton");
        tester.assertNoErrorMessage();
        ConnectorDescription description = serviceManager.getAttributeValues(testdomainConnectorId);
        assertThat(DictionaryAsMap.wrap(description.getProperties()), hasEntry("location.bar", (Object) globTest));
    }
    
    @Test
    public void wire_UpdateExistingLocation_shouldUpdateServiceProperties() {
        selectDomain(1); //TestDomainInterface
        selectFirstGlobal();
        selectFirstEndpoint();
        ContextHolder.get().setCurrentContextId("one");
        tester.clickLink("wiringForm:wireButton");
        tester.assertNoErrorMessage();
        ConnectorDescription description = serviceManager.getAttributeValues(testdomainConnectorId);
        Map<String, Object> props = DictionaryAsMap.wrap(description.getProperties());
        assertThat(props, hasKey("location.one"));
        Object[] array = (Object[]) props.get("location.one");
        assertThat(array.length, is(2));
        assertThat(array, hasItemInArray((Object) globTest));
    }
    
    @Test
    public void wire_UpdateExistingLocationArray_shouldUpdateServiceProperties() {
        selectDomain(1); //TestDomainInterface
        selectFirstGlobal();
        selectFirstEndpoint();
        ContextHolder.get().setCurrentContextId("two");
        tester.clickLink("wiringForm:wireButton");
        tester.assertNoErrorMessage();
        ConnectorDescription description = serviceManager.getAttributeValues(testdomainConnectorId);
        Map<String, Object> props = DictionaryAsMap.wrap(description.getProperties());
        assertThat(props, hasKey("location.two"));
        Object[] array = (Object[]) props.get("location.two");
        assertThat(array.length, is(3));
        assertThat(array, hasItemInArray((Object) globTest));
    }
    
    @Test
    public void wire_theSameGlobalTwoTimes_shouldNotUpdateServiceProperties() {
        ConnectorDescription olddescription = serviceManager.getAttributeValues(testdomainConnectorId);
        selectDomain(1); //TestDomainInterface
        selectFirstGlobal();
        selectFirstEndpoint();
        ContextHolder.get().setCurrentContextId("twotimes1");
        tester.clickLink("wiringForm:wireButton");
        ConnectorDescription description = serviceManager.getAttributeValues(testdomainConnectorId);
        assertThat(description.getProperties().get("twotimes1"), is(olddescription.getProperties().get("twotimes1")));
    }
    
    @Test
    public void wire_theSameGlobalTwoTimes_Array_shouldNotUpdateServiceProperties() {
        ConnectorDescription olddescription = serviceManager.getAttributeValues(testdomainConnectorId);
        selectDomain(1); //TestDomainInterface
        selectFirstGlobal();
        selectFirstEndpoint();
        ContextHolder.get().setCurrentContextId("twotimes2");
        tester.clickLink("wiringForm:wireButton");
        ConnectorDescription description = serviceManager.getAttributeValues(testdomainConnectorId);
        assertThat(description.getProperties().get("twotimes2"), is(olddescription.getProperties().get("twotimes2")));
    }
    
    private void selectDomain(int index) {
        FormTester formTester = tester.newFormTester("domainChooseForm");
        formTester.select("domains", index);
        formTester.submit();
        tester.executeAjaxEvent("domainChooseForm:domains", "onchange");
    }

    private void selectFirstGlobal() {
        tester.clickLink("globals:i:3:nodeComponent:contentLink");
    }
    
    private void selectFirstEndpoint() {
        tester.clickLink("endpoints:i:3:nodeComponent:contentLink");
    }
    
    private void createConnectors() throws Exception {
        createProviderMocks();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("value", "42");
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("location.root", "domain/testdomain/default");
        properties.put("location.one", "bar");
        properties.put("location.two", new Object[] { "foo", "bar" });
        properties.put("location.twotimes1", globTest);
        properties.put("location.twotimes2", new Object[] { "foo", globTest });
        testdomainConnectorId = new ConnectorId("testdomain", "testconnector", "test-service"); 
        serviceManager.create(testdomainConnectorId, new ConnectorDescription(attributes, properties));
    }

    private void createProviderMocks() {
        createDomainProviderMock(TestDomainInterface.class, "testdomain");
        createDomainProviderMock(AnotherTestDomainInterface.class, "anotherTestDomain");
        createConnectorProviderMock("testconnector", "testdomain");
        ConnectorInstanceFactory factory = mock(ConnectorInstanceFactory.class);
        when(factory.createNewInstance(anyString())).thenAnswer(new Answer<Domain>() {
            @Override
            public Domain answer(InvocationOnMock invocation) throws Throwable {
                TestDomainInterface newMock = mock(TestDomainInterface.class);
                when(newMock.getInstanceId()).thenReturn((String) invocation.getArguments()[0]);
                return newMock;
            }
        });
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.DOMAIN_KEY, "testdomain");
        props.put(Constants.CONNECTOR_KEY, "testconnector");
        registerService(factory, props, ConnectorInstanceFactory.class);
    }
    
    private static class IsAssignableFrom extends TypeSafeMatcher<Class<?>> {
        private Class<?> clazz;
        
        public IsAssignableFrom(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public boolean matchesSafely(Class<?> toMatch) {
            return clazz.isAssignableFrom(toMatch);
        }

        public void describeTo(Description description) {
            description.appendText("a class assignable from ").appendText(clazz.getCanonicalName());
        }

        @Factory
        public static <T> Matcher<Class<?>> isAssignableFrom(Class<?> clazz) {
            return new IsAssignableFrom(clazz);
        }
    }
}
