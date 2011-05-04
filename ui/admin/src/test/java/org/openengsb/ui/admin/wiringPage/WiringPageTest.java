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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
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
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.common.util.DictionaryAsMap;
import org.openengsb.core.common.util.DictionaryUtils;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.wiringPage.WiringPage.CheckedTree;

public class WiringPageTest extends AbstractUITest {

    public interface TestDomainInterface extends Domain {
    }
    public interface AnotherTestDomainInterface extends Domain {
    }
    
    private RuleManager ruleManager;
    
    private ConnectorId testdomainConnectorId;
    private final String globTest = "globTest";
    private final String anotherGlob = "anotherGlob";
    private Dictionary<String, Object> startproperties;
    
    @Before
    public void setUp() throws Exception {
        ruleManager = mock(RuleManager.class);
        Map<String, String> globals = new TreeMap<String, String>();
        globals.put(globTest, TestDomainInterface.class.getCanonicalName());
        globals.put(anotherGlob, AnotherTestDomainInterface.class.getCanonicalName());
        when(ruleManager.listGlobals()).thenReturn(globals);
        when(ruleManager.getGlobalType(globTest)).thenReturn(globals.get(globTest));
        when(ruleManager.getGlobalType(anotherGlob)).thenReturn(globals.get(anotherGlob));
        context.putBean(ruleManager);
        List<String> contextList = new ArrayList<String>();
        contextList.add("bar");
        contextList.add("one");
        contextList.add("two");
        contextList.add("twotimes1");
        contextList.add("twotimes2");
        contextList.add("foo");
        ContextCurrentService contextService = 
            (ContextCurrentService) context.getBean((mock(ContextCurrentService.class)).getClass().getName());
        when(contextService.getAvailableContexts()).thenReturn(contextList);
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
        tester.assertInvisible("globals");
        tester.assertInvisible("endpoints");
        tester.assertComponent("wiringForm", Form.class);
        tester.assertComponent("wiringForm:globalName", TextField.class);
        tester.assertComponent("wiringForm:wireButton", AjaxSubmitLink.class);
        tester.assertComponent("wiringForm:instanceId", TextField.class);
        tester.assertComponent("feedbackPanel", FeedbackPanel.class);
        tester.assertComponent("wiringForm:contextList", CheckedTree.class);
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
    public void contextList_shouldBeLoaded() {
        CheckedTree globals = (CheckedTree) tester.getComponentFromLastRenderedPage("wiringForm:contextList");
        TreeModel tree = globals.getModelObject();
        assertThat(tree.getChildCount(tree.getRoot()), is(6));
        assertThat(tree.getChild(tree.getRoot(), 0).toString(), is("bar"));
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
    public void selectEndpoint_shouldUpdateLabel() {
        selectDomain(1);
        selectFirstEndpoint();
        TextField<?> epLabel = (TextField<?>) tester.getComponentFromLastRenderedPage("wiringForm:instanceId");
        assertThat(epLabel.getDefaultModelObject().toString(), is(testdomainConnectorId.toFullID()));
    }

    @Test
    public void wire_NewLocationProperty_shouldUpdateServiceProperties() {
        selectDomain(1); //TestDomainInterface
        selectFirstEndpoint();
        selectContext(1); //bar
        setGlobal(globTest);
        tester.clickLink("wiringForm:wireButton");
        tester.assertNoErrorMessage();
        ConnectorDescription description = serviceManager.getAttributeValues(testdomainConnectorId);
        assertThat(DictionaryAsMap.wrap(description.getProperties()), hasEntry("location.bar", (Object) globTest));
    }

    @Test
    public void wire_UpdateExistingLocation_shouldUpdateServiceProperties() {
        selectDomain(1); //TestDomainInterface
        selectFirstEndpoint();
        setGlobal(globTest);
        selectContext(2); //one
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
        selectFirstEndpoint();
        setGlobal(globTest);
        selectContext(3); //two
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
        selectDomain(1); //TestDomainInterface
        selectFirstEndpoint();
        setGlobal(globTest);
        selectContext(4); //twotimes1
        tester.clickLink("wiringForm:wireButton");
        tester.assertNoErrorMessage();
        ConnectorDescription description = serviceManager.getAttributeValues(testdomainConnectorId);
        assertThat(description.getProperties().get("location.twotimes1"), 
            is(startproperties.get("location.twotimes1")));
    }
    
    @Test
    public void wire_theSameGlobalTwoTimes_Array_shouldNotUpdateServiceProperties() {
        selectDomain(1); //TestDomainInterface
        selectFirstEndpoint();
        setGlobal(globTest);
        selectContext(5); //twotimes2
        tester.clickLink("wiringForm:wireButton");
        tester.assertNoErrorMessage();
        ConnectorDescription description = serviceManager.getAttributeValues(testdomainConnectorId);
        assertThat(description.getProperties().get("location.twotimes2"), 
            is(startproperties.get("location.twotimes2")));
    }
    
    @Test
    public void wire_NewGlobal_shouldUpdateServiceProperties() {
        selectDomain(1); //TestDomainInterface
        selectFirstEndpoint();
        setGlobal("newGlob");
        selectContext(1); //bar
        tester.clickLink("wiringForm:wireButton");
        tester.assertNoErrorMessage();
        verify(ruleManager).addGlobal(TestDomainInterface.class.getCanonicalName(), "newGlob");
        ConnectorDescription description = serviceManager.getAttributeValues(testdomainConnectorId);
        assertThat(DictionaryAsMap.wrap(description.getProperties()), hasEntry("location.bar", (Object) "newGlob"));
    }
    
    @Test
    public void wire_ExistingGlobal_shouldNotUpdateServiceProperties() {
        selectDomain(1); //TestDomainInterface
        selectFirstEndpoint();
        setGlobal(anotherGlob);
        selectContext(2); //one
        tester.clickLink("wiringForm:wireButton");
        tester.assertNoErrorMessage();
        ConnectorDescription description = serviceManager.getAttributeValues(testdomainConnectorId);
        assertThat(description.getProperties().get("location.one"), is(startproperties.get("location.one")));
    }
    
    private void selectDomain(int index) {
        FormTester formTester = tester.newFormTester("domainChooseForm");
        formTester.select("domains", index);
        formTester.submit();
        tester.executeAjaxEvent("domainChooseForm:domains", "onchange");
    }
    
    @Test
    public void wire_MultipleContexts_shouldUpdateServiceProperties() {
        selectDomain(1); //TestDomainInterface
        selectFirstEndpoint();
        setGlobal(globTest);
        selectContext(1); //bar
        selectContext(6); //foo
        tester.clickLink("wiringForm:wireButton");
        tester.assertNoErrorMessage();
        ConnectorDescription description = serviceManager.getAttributeValues(testdomainConnectorId);
        assertThat(DictionaryAsMap.wrap(description.getProperties()), hasEntry("location.bar", (Object) globTest));
        assertThat(DictionaryAsMap.wrap(description.getProperties()), hasEntry("location.foo", (Object) globTest));
    }
    
    private void setGlobal(String global) {
        tester.setParameterForNextRequest("wiringForm:globalName", global);
    }

    private void selectFirstGlobal() {
        tester.clickLink("globals:i:1:nodeComponent:contentLink");
    }
    
    private void selectFirstEndpoint() {
        tester.clickLink("endpoints:i:1:nodeComponent:contentLink");
    }
    
    private void selectContext(int i) {
        tester.setParameterForNextRequest("wiringForm:contextList:i:" + i + ":nodeComponent:check", true);
    }
    
    private void createConnectors() throws Exception {
        createProviderMocks();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("value", "42");
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("location.root", "domain/testdomain/default");
        properties.put("location.one", "bar");
        properties.put("location.two", new Object[] { "foo", "bar" });
        properties.put("location.twotimes1", globTest);
        properties.put("location.twotimes2", new Object[] { "foo", globTest });
        startproperties = DictionaryUtils.copy(properties);
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
