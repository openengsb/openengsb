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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.ui.admin.AbstractLoginTest;

public class ProjectTest extends AbstractLoginTest {
    private Page basePage;

    @Before
    public void setup() {
        /*
         * this line should be reconsidered as soon as the root-context is implemented [OPENENGSB-974]
         */
        ContextHolder.get().setCurrentContextId(null);

        // Maybe there is a more elegant way to do this...
        AuthenticatedWebSession session = AuthenticatedWebSession.get();
        session.signIn("test", "password");

        when(contextCurrentService.getAvailableContexts()).thenReturn(Arrays.asList(new String[]{ "foo", "bar" }));

        basePage = tester.startPage(new DummyPage());
    }

    @Test
    public void testIfLabelIsPresent_shouldContainLabelString() throws Exception {
        String labelString =
            tester.getApplication().getResourceSettings().getLocalizer().getString("project.choice.label",
                basePage.get("header"));
        tester.assertContains(labelString);
    }

    @Test
    public void testInitDefaultContext_shouldSetFooContext() throws Exception {
        tester.assertComponent("projectChoiceForm:projectChoice", DropDownChoice.class);
        assertThat(ContextHolder.get().getCurrentContextId(), is("foo"));
    }

    @Test
    public void testChangeContextDropdown_shouldChangeThreadlocal() throws Exception {
        tester.assertComponent("projectChoiceForm:projectChoice", DropDownChoice.class);
        assertThat(ContextHolder.get().getCurrentContextId(), is("foo"));

        FormTester formTester = tester.newFormTester("projectChoiceForm");
        formTester.select("projectChoice", 1);

        // simulated page reload...
        tester.startPage(new DummyPage());
        assertThat("bar", is(ContextHolder.get().getCurrentContextId()));
    }
}
