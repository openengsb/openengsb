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

import org.apache.wicket.util.tester.WicketTester;
import org.openengsb.core.api.ServiceRegistrationManager;
import org.openengsb.core.api.descriptor.AttributeDefinition;

public class EditorPageTest {

    private AttributeDefinition attrib1;
    private ServiceRegistrationManager manager;
    private WicketTester tester;
//
//    @Before
//    public void setup() {
//        tester = new WicketTester();
//        manager = mock(InternalServiceRegistrationManager.class);
//        attrib1 =
//            AttributeDefinition.builder(new PassThroughStringLocalizer()).id("a").defaultValue("a_default")
//                .name("a_name").build();
//        ServiceDescriptor d =
//            ServiceDescriptor.builder(new PassThroughStringLocalizer()).serviceType(NullDomain.class)
//                .implementationType(NullDomainImpl.class).id("a").name("sn").description("sd").attribute(attrib1)
//                .build();
//        when(manager.getDescriptor()).thenReturn(d);
//    }
//
//    @Test
//    public void attributesWithDefaultValues_shouldInitializeModelWithDefaults() throws Exception {
//        ConnectorEditorPage page = new ConnectorEditorPage(manager);
//        assertThat(page.getEditorPanel().getValues().get("a"), is("a_default"));
//    }
//
//    @Test
//    public void testIfValuesOfAttributesAreShown() {
//
//        Map<String, String> attributes = new HashMap<String, String>();
//        attributes.put("a", "testValue");
//        when(manager.getAttributeValues("a")).thenReturn(attributes);
//
//        ConnectorEditorPage page = new ConnectorEditorPage(manager, "a");
//        tester.startPage(page);
//        tester.debugComponentTrees();
//
//        assertThat(page.getEditorPanel().getAttributes().size(), is(1));
//        assertThat(page.getEditorPanel().getAttributes().get(0).getId(), is("a"));
//    }
//
//    @SuppressWarnings("unchecked")
//    @Test
//    public void testIdFieldIsEditable() {
//        ConnectorEditorPage page = new ConnectorEditorPage(manager);
//        tester.startPage(page);
//        tester.debugComponentTrees();
//        TextField<String> idField =
//            (TextField<String>) tester.getComponentFromLastRenderedPage("editor:form:serviceId");
//        assertThat(idField.isEnabled(), is(true));
//    }
//
//    @SuppressWarnings({ "unchecked", "serial" })
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
//    @Test
//    @SuppressWarnings({ "unchecked", "serial" })
//    @Ignore("OPENENGSB-277, what checks should be bypassed")
//    public void uncheckValidationCheckbox_shouldBypassValidation() {
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
//        formTester.setValue("validate", false);
//        formTester.submit();
//        tester.assertErrorMessages(new String[]{});
//        tester.assertInfoMessages(new String[]{ "Service can be added" });
//        Mockito.verify(manager).update(Mockito.anyString(), Mockito.anyMap());
//        Mockito.verify(manager, Mockito.never()).update(Mockito.anyString(), Mockito.anyMap());
//    }
}
