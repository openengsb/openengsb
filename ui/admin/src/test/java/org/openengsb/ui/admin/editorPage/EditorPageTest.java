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

package org.openengsb.ui.admin.editorPage;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.l10n.PassThroughStringLocalizer;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.connectorEditorPage.ConnectorEditorPage;

public class EditorPageTest extends AbstractUITest {

    private AttributeDefinition attrib1;

    @Before
    public void setup() {
        attrib1 =
            AttributeDefinition.builder(new PassThroughStringLocalizer()).id("a").defaultValue("a_default")
                .name("a_name").build();
        ServiceDescriptor d =
            ServiceDescriptor.builder(new PassThroughStringLocalizer()).implementationType(NullDomainImpl.class)
                .id("a").name("sn").description("sd").attribute(attrib1)
                .build();

        ConnectorProvider provider = createConnectorProviderMock("testconnector", "testdomain");
        when(provider.getDescriptor()).thenReturn(d);
        createDomainProviderMock(NullDomain.class, "testdomain");
        createFactoryMock("testconnector", "testdomain");
        tester.getApplication().addComponentInstantiationListener(
            new SpringComponentInjector(tester.getApplication(), context, false));
    }

    @Test
    public void attributesWithDefaultValues_shouldInitializeModelWithDefaults() throws Exception {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        RepeatingView list =
            (RepeatingView) tester.getComponentFromLastRenderedPage("editor:form:attributesPanel:fields");
        @SuppressWarnings("unchecked")
        TextField<String> component = (TextField<String>) list.get("a:row:field");
        assertThat(component.getModelObject(), is("a_default"));
        tester.debugComponentTrees();
    }

    @Test
    public void testIfValuesOfAttributesAreShown() throws Exception {
        ConnectorId connectorId = ConnectorId.generate("testdomain", "testconnector");
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("a", "testValue");
        serviceManager.createService(connectorId, new ConnectorDescription(attributes));
        PageParameters pageParams =
            new PageParameters("domainType=testdomain,connectorType=testconnector,id=" + connectorId.getInstanceId());
        tester.startPage(ConnectorEditorPage.class, pageParams);
        FormComponentLabel nameLabel =
            (FormComponentLabel) tester
                .getComponentFromLastRenderedPage("editor:form:attributesPanel:fields:a:row:name");
        assertThat(nameLabel.getDefaultModelObjectAsString(), is("a_name"));
        @SuppressWarnings("unchecked")
        TextField<String> value =
            (TextField<String>) tester
                .getComponentFromLastRenderedPage("editor:form:attributesPanel:fields:a:row:field");
        assertThat(value.getValue(), is("testValue"));
    }

    @Test
    public void testIdFieldIsEditable() {
        tester.startPage(new ConnectorEditorPage("testdomain", "testconnector"));
        tester.debugComponentTrees();
        @SuppressWarnings("unchecked")
        TextField<String> idField =
            (TextField<String>) tester.getComponentFromLastRenderedPage("editor:form:serviceId");
        assertThat(idField.isEnabled(), is(true));
    }

    // @SuppressWarnings({ "unchecked", "serial" })
//    public void addServiceManagerValidationError_ShouldPutErrorMessagesOnPage() {
//        Map<String, String> errorMessages = new HashMap<String, String>();
//        errorMessages.put("a", "validation.service.not");
//        when(manager.update(Mockito.anyString(), Mockito.anyMap())).thenReturn(
//            new MultipleAttributeValidationResultImpl(false, errorMessages));
//        WicketTester tester = new WicketTester();
//        tester.startPage(new ITestPageSource() {
//            @Override
//            public Page getTestPage() {
//                return new ConnectorEditorPage(manager);
//            }
//        });
//        FormTester formTester = tester.newFormTester("editor:form");
//        formTester.setValue("fields:id:row:field", "someValue");
//        formTester.submit();
//        tester.assertErrorMessages(new String[]{ "Service Validation Error" });
//        tester.assertRenderedPage(ConnectorEditorPage.class);
//    }
    //
    // @Test
    // @SuppressWarnings({ "unchecked", "serial" })
    // @Ignore("OPENENGSB-277, what checks should be bypassed")
    // public void uncheckValidationCheckbox_shouldBypassValidation() {
    // Map<String, String> errorMessages = new HashMap<String, String>();
    // errorMessages.put("a", "validation.service.not");
    // when(manager.update(Mockito.anyString(), Mockito.anyMap())).thenReturn(
    // new MultipleAttributeValidationResultImpl(false, errorMessages));
    // WicketTester tester = new WicketTester();
    // tester.startPage(new ITestPageSource() {
    // @Override
    // public Page getTestPage() {
    // return new ConnectorEditorPage(manager);
    // }
    // });
    // FormTester formTester = tester.newFormTester("editor:form");
    // formTester.setValue("fields:id:row:field", "someValue");
    // formTester.setValue("validate", false);
    // formTester.submit();
    // tester.assertErrorMessages(new String[]{});
    // tester.assertInfoMessages(new String[]{ "Service can be added" });
    // Mockito.verify(manager).update(Mockito.anyString(), Mockito.anyMap());
    // Mockito.verify(manager, Mockito.never()).update(Mockito.anyString(), Mockito.anyMap());
    // }
}
