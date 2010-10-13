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

package org.openengsb.ui.web;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.ITestPageSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.l10n.PassThroughStringLocalizer;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;

public class EditorPageTest {

    private static class DummyDomain implements Domain {
        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }
    }

    private AttributeDefinition attrib1;
    private ServiceManager manager;
    private WicketTester tester;

    @Before
    public void setup() {
        tester = new WicketTester();
        manager = mock(ServiceManager.class);
        attrib1 = AttributeDefinition.builder(new PassThroughStringLocalizer()).id("a").defaultValue("a_default")
                .name("a_name").build();
        ServiceDescriptor d = ServiceDescriptor.builder(new PassThroughStringLocalizer())
                .serviceType(DummyDomain.class).implementationType(DummyDomain.class)
                .id("a").name("sn").description("sd").attribute(attrib1).build();
        when(manager.getDescriptor()).thenReturn(d);
    }

    @Test
    public void attributesWithDefaultValues_shouldInitializeModelWithDefaults() throws Exception {
        ConnectorEditorPage page = new ConnectorEditorPage(manager);
        assertThat(page.getEditorPanel().getValues().get("a"), is("a_default"));
    }

    @Test
    public void shouldAddAnIdAttributeAtBeginning() throws Exception {
        ConnectorEditorPage page = new ConnectorEditorPage(manager);
        assertThat(page.getEditorPanel().getAttributes().size(), is(2));
        assertThat(page.getEditorPanel().getAttributes().get(0).getId(), is("id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIfValuesOfAttributesAreShown() {

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("id", "id1");
        when(manager.getAttributeValues("a")).thenReturn(attributes);

        ConnectorEditorPage page = new ConnectorEditorPage(manager, "a");
        tester.startPage(page);
        tester.debugComponentTrees();
        TextField<String> idField = (TextField<String>) tester
                .getComponentFromLastRenderedPage("editor:form:fields:id:row:field");
        assertThat(page.getEditorPanel().getAttributes().size(), is(2));
        assertThat(page.getEditorPanel().getAttributes().get(0).getId(), is("id"));
        Assert.assertEquals("id1", idField.getDefaultModel().getObject());
    }

    @SuppressWarnings({ "unchecked", "serial" })
    public void addServiceManagerValidationError_ShouldPutErrorMessagesOnPage() {
        Map<String, String> errorMessages = new HashMap<String, String>();
        errorMessages.put("a", "validation.service.not");
        when(manager.update(Mockito.anyString(), Mockito.anyMap())).thenReturn(
                new MultipleAttributeValidationResultImpl(false, errorMessages));
        WicketTester tester = new WicketTester();
        tester.startPage(new ITestPageSource() {
            @Override
            public Page getTestPage() {
                return new ConnectorEditorPage(manager);
            }
        });
        FormTester formTester = tester.newFormTester("editor:form");
        formTester.setValue("fields:id:row:field", "someValue");
        formTester.submit();
        tester.assertErrorMessages(new String[]{ "Service Validation Error" });
        tester.assertRenderedPage(ConnectorEditorPage.class);
    }

    @Test
    @SuppressWarnings({ "unchecked", "serial" })
    @Ignore("OPENENGSB-277, what checks should be bypassed")
    public void uncheckValidationCheckbox_shouldBypassValidation() {
        Map<String, String> errorMessages = new HashMap<String, String>();
        errorMessages.put("a", "validation.service.not");
        when(manager.update(Mockito.anyString(), Mockito.anyMap())).thenReturn(
                new MultipleAttributeValidationResultImpl(false, errorMessages));
        WicketTester tester = new WicketTester();
        tester.startPage(new ITestPageSource() {
            @Override
            public Page getTestPage() {
                return new ConnectorEditorPage(manager);
            }
        });
        FormTester formTester = tester.newFormTester("editor:form");
        formTester.setValue("fields:id:row:field", "someValue");
        formTester.setValue("validate", false);
        formTester.submit();
        tester.assertErrorMessages(new String[]{});
        tester.assertInfoMessages(new String[]{ "Service can be added" });
        Mockito.verify(manager).update(Mockito.anyString(), Mockito.anyMap());
        Mockito.verify(manager, Mockito.never()).update(Mockito.anyString(), Mockito.anyMap());
    }
}
