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
package org.openengsb.config.test.unit.view;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.junit.Test;
import org.openengsb.config.WicketBase;
import org.openengsb.config.view.CreateAssemblyPage;
import org.openengsb.config.view.util.ChoiceOption;

public class CreateAssemblyPageTest extends WicketBase {
    @Test
    @SuppressWarnings("unchecked")
    public void dropDownChoice_shouldListEndpointsAndBeans() throws Exception {
        tester.startPage(CreateAssemblyPage.class);
        DropDownChoice<ChoiceOption> choice = extTester.assertEnabled("form:suSelect", DropDownChoice.class);
        assertThat(choice.getChoices().size(), is(2));
        assertThat(choice.getChoices().get(0).getId(), is("test-connector:test"));
        assertThat(choice.getChoices().get(1).getId(), is("test-connector:connector.test.TestBean"));
    }

    @Test
    public void onEmptyLists_shouldBeInvisibleAndLabelsVisible() throws Exception {
        tester.startPage(CreateAssemblyPage.class);
        extTester.assertVisible("endpointsEmpty");
        extTester.assertInvisible("serviceUnits");
        extTester.assertVisible("beansEmpty");
        extTester.assertInvisible("beans");
    }
}
