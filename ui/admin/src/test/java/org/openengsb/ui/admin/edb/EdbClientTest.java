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
package org.openengsb.ui.admin.edb;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.ekb.api.EDBQueryFilter;
import org.openengsb.core.ekb.api.EKBService;
import org.openengsb.core.ekb.api.SingleModelQuery;
import org.openengsb.core.test.DummyModel;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.ServiceList;
import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.Constants;
import org.openengsb.labs.delegation.service.internal.ClassProviderImpl;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.index.Index;

public class EdbClientTest extends AbstractUITest {

    private EKBService ekbService;

    @Before
    public void setUp() throws Exception {
        createDomainProviderMock(NullDomain.class, "example");
        ekbService = mock(EKBService.class);
        context.putBean("queryInterface", ekbService);
        DummyModel dummyModel = new DummyModel();
        dummyModel.setId("42");
        dummyModel.setValue("foo");
        ekbService.query(new SingleModelQuery(DummyModel.class, new EDBQueryFilter("id:42"), null));
        // TODO: check @FJE
        // when(ekbService.query(new SingleModelQuery(DummyModel.class, new
        // EDBQueryFilter("id:42"), null))).thenReturn(
        // Arrays.asList(dummyModel));
        when(ekbService.query(new SingleModelQuery(DummyModel.class, new EDBQueryFilter("crap"), null))).thenThrow(
                new IllegalArgumentException("illegal query"));
        ServiceList<ClassProvider> classProviders = super.makeServiceList(ClassProvider.class);
        context.putBean("modelProviders", classProviders);
        ClassProviderImpl classProvider = new ClassProviderImpl(bundle, DummyModel.class.getName());
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.DELEGATION_CONTEXT_KEY, org.openengsb.core.api.Constants.DELEGATION_CONTEXT_MODELS);
        registerService(classProvider, props, ClassProvider.class);
    }

    @Test
    public void testShowIndex_shouldContainEDBClientPage() throws Exception {
        tester.startPage(Index.class);
        tester.clickLink("menu:menuItems:7:link");
        tester.assertRenderedPage(EdbClient.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testModelDropDown_shouldContainDummyModel() throws Exception {
        tester.startPage(EdbClient.class);
        DropDownChoice<Class<? extends OpenEngSBModel>> dropdown = (DropDownChoice<Class<? extends OpenEngSBModel>>) tester
                .getComponentFromLastRenderedPage("form:modelSelector");
        @SuppressWarnings("rawtypes")
        List<Object> choices = (List) dropdown.getChoices();
        assertThat(choices, hasItem((Object) DummyModel.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSelectModel_shouldEnableQueryField() throws Exception {
        tester.startPage(EdbClient.class);
        Component query = tester.getComponentFromLastRenderedPage("form:query");
        assertFalse("queryfield not disabled at the beginning", query.isEnabled());
        FormTester formTester = tester.newFormTester("form");
        DropDownChoice<Class<? extends OpenEngSBModel>> modeldropdown = (DropDownChoice<Class<? extends OpenEngSBModel>>) tester
                .getComponentFromLastRenderedPage("form:modelSelector");
        formTester.select("modelSelector", getIndexForValue(modeldropdown, "DummyModel"));
        tester.executeAjaxEvent(modeldropdown, "onchange");
        assertTrue("QueryField not enabled after selection", query.isEnabled());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEnterQuery_shouldReturnQueryResults() throws Exception {
        tester.startPage(EdbClient.class);
        FormTester formTester = tester.newFormTester("form");
        DropDownChoice<Class<? extends OpenEngSBModel>> modeldropdown = (DropDownChoice<Class<? extends OpenEngSBModel>>) tester
                .getComponentFromLastRenderedPage("form:modelSelector");
        formTester.select("modelSelector", getIndexForValue(modeldropdown, "DummyModel"));
        tester.executeAjaxEvent(modeldropdown, "onchange");
        formTester.setValue("query", "id:42");
        tester.executeAjaxEvent("form:submit", "onclick");
        ListView<? extends OpenEngSBModel> resultElement = (ListView<? extends OpenEngSBModel>) tester
                .getComponentFromLastRenderedPage("result:list");
        tester.assertFeedback("form:feedback", "Found 1 results");
        assertThat(resultElement.get("0:id").getDefaultModelObjectAsString(), is("42"));
        assertThat(resultElement.get("0:entries").getDefaultModelObjectAsString(), containsString("foo"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInvalidQuery_shouldShowError() throws Exception {
        tester.startPage(EdbClient.class);
        FormTester formTester = tester.newFormTester("form");
        DropDownChoice<Class<? extends OpenEngSBModel>> modeldropdown = (DropDownChoice<Class<? extends OpenEngSBModel>>) tester
                .getComponentFromLastRenderedPage("form:modelSelector");
        formTester.select("modelSelector", getIndexForValue(modeldropdown, "DummyModel"));
        tester.executeAjaxEvent(modeldropdown, "onchange");
        formTester.setValue("query", "crap");
        tester.executeAjaxEvent("form:submit", "onclick");
        tester.assertFeedback(
                "form:feedback",
                String.format("Error when querying for models %s (%s)", "illegal query",
                        IllegalArgumentException.class.getName()));
    }
}
