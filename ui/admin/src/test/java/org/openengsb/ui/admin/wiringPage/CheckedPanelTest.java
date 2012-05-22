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
import static org.junit.Assert.assertThat;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class CheckedPanelTest {

    private WicketTester tester;
    private Model<Boolean> checkModel;
    private Model<String> labelModel;

    @Before
    public void setUp() {
        checkModel = new Model<Boolean>();
        checkModel.setObject(Boolean.FALSE);

        labelModel = new Model<String>();
        labelModel.setObject("testlabel");

        tester = new WicketTester();
        tester.startComponentInPage(new CheckedPanel("panel", checkModel, labelModel));
    }

    @Test
    public void testRendered() {
        tester.debugComponentTrees();
        tester.assertComponent("panel:check", CheckBox.class);
        tester.assertComponent("panel:label", SimpleFormComponentLabel.class);
    }

    @Test
    public void testLabel_shouldShowLabelModelObject() {
        SimpleFormComponentLabel label = (SimpleFormComponentLabel)
            tester.getComponentFromLastRenderedPage("panel:label");
        assertThat(label.getDefaultModelObjectAsString(), is("testlabel"));
    }

    @Test
    public void testAttributeOfLabel_shouldBeIdOfTheCheckBox() {
        CheckBox checkBox = (CheckBox)
            tester.getComponentFromLastRenderedPage("panel:check");
        TagTester tagTester = tester.getTagByWicketId("label");
        assertThat(tagTester.hasAttribute("for"), is(true));
        assertThat(tagTester.getAttribute("for"), is(checkBox.getMarkupId()));
    }

}
