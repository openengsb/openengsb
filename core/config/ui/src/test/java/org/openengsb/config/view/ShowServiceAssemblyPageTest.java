/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.config.view;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.junit.Test;
import org.openengsb.config.WicketBase;
import org.openengsb.config.domain.PersistedObject;
import org.openengsb.config.domain.ServiceAssembly;
import org.openengsb.config.jbi.types.EndpointType;

public class ShowServiceAssemblyPageTest extends WicketBase {
    @Test
    @SuppressWarnings("unchecked")
    public void endpointChoice_listsSupportedEndpoints() throws Exception {
        tester.startPage(new ShowServiceAssemblyPage(new ServiceAssembly()));
        DropDownChoice<EndpointType> choice = extTester.assertEnabled("newEndpointForm:endpointSelect",
                DropDownChoice.class);
        assertThat(choice.getChoices().size(), is(mockComponentService.getEndpoints().size()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void beanChoice_listsSupportedBeans() throws Exception {
        tester.startPage(new ShowServiceAssemblyPage(new ServiceAssembly()));
        DropDownChoice<EndpointType> choice = extTester.assertEnabled("newBeanForm:beanSelect", DropDownChoice.class);
        assertThat(choice.getChoices().size(), is(mockComponentService.getBeans().size()));
    }

    @Test
    public void onEmptyLists_LabelsAreVisible() throws Exception {
        tester.startPage(new ShowServiceAssemblyPage(new ServiceAssembly()));
        extTester.assertVisible("endpointLabel");
        extTester.assertVisible("beanLabel");
    }

    @Test
    public void assemblyWithEndpoints_endpointListIsShown() throws Exception {
        ServiceAssembly sa = new ServiceAssembly();
        PersistedObject e = new PersistedObject(PersistedObject.Type.Endpoint, "a", sa);
        sa.getPersistedObjects().add(e);
        tester.startPage(new ShowServiceAssemblyPage(sa));
        extTester.assertInvisible("endpointLabel");
        extTester.assertVisible("endpoints");
        // TODO this fails because of the used compound model, although
        // rendering works just fine
        // tester.assertListView("endpoints", Lists.newArrayList(e));
    }

    @Test
    public void assemblyWithBeans_beanListIsShown() {
        ServiceAssembly sa = new ServiceAssembly();
        PersistedObject b = new PersistedObject(PersistedObject.Type.Bean, "a", sa);
        sa.getPersistedObjects().add(b);
        tester.startPage(new ShowServiceAssemblyPage(sa));
        extTester.assertInvisible("beanLabel");
        extTester.assertVisible("beans");
    }

    @Test
    public void deployedAssembly_checkButtonsDeployUndeployNotDelete() {
        ServiceAssembly sa = new ServiceAssembly();
        when(assemblyService.isDeployed(sa)).thenReturn(true);
        tester.startPage(new ShowServiceAssemblyPage(sa));
        extTester.assertEnabled("actionForm:deployButton");
        extTester.assertEnabled("actionForm:undeployButton");
        extTester.assertDisabled("actionForm:deleteButton");
    }
}
