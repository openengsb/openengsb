/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.ui.web;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.core.config.descriptor.ServiceDescriptor;

public class EditorPageTest {

    private AttributeDefinition attrib1;
    private ServiceManager manager;

    @Before
    public void setup() {
        new WicketTester();
        manager = mock(ServiceManager.class);
        attrib1 = new AttributeDefinition();
        attrib1.setId("a");
        attrib1.setDefaultValue("a_default");
        attrib1.setName("a_name");
        ServiceDescriptor d = new ServiceDescriptor();
        d.setId("a");
        d.setName("sn");
        d.setDescription("sd");
        d.addAttribute(attrib1);
        when(manager.getDescriptor(Mockito.any(Locale.class))).thenReturn(d);
    }

    @Test
    public void attributesWithDefaultValues_shouldInitializeModelWithDefaults() throws Exception {
        EditorPage page = new EditorPage(manager);
        assertThat(page.getEditorPanel().getValues().get("a"), is("a_default"));
    }

    @Test
    public void shouldAddAnIdAttributeAtBeginning() throws Exception {
        EditorPage page = new EditorPage(manager);
        assertThat(page.getEditorPanel().getAttributes().size(), is(2));
        assertThat(page.getEditorPanel().getAttributes().get(0).getId(), is("id"));
    }
}
