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
package org.openengsb.config;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTester;

public class ExtWicketTester {
    protected WicketTester tester;

    public ExtWicketTester(WicketTester tester) {
        this.tester = tester;
    }

    /**
     * Clicks the link by the given path in the last rendered page. Asserts that
     * the given path is of type {@link Link} and is visible.
     *
     * @param path path to {@link Link} in last rendered page
     */
    public void clickLink(String path) {
        assertEnabled(path, Link.class);
        tester.clickLink(path);
    }

    /**
     * Asserts that a component is visible and enabled.
     *
     * @param path path to component in last rendered page
     */
    public Component assertEnabled(String path) {
        Component component = tester.getComponentFromLastRenderedPage(path);
        assertThat("component is not visible", component, notNullValue());
        assertThat(component.isEnabled(), is(true));
        return component;
    }

    @SuppressWarnings("unchecked")
    public <T> T assertEnabled(String path, Class<T> componentClass) {
        Component component = assertEnabled(path);
        assertThat(component, is(componentClass));
        return (T) component;
    }

    /**
     * Asserts that a component is visible but disabled.
     *
     * @param path path to component in last rendered page
     */
    public Component assertDisabled(String path) {
        Component component = tester.getComponentFromLastRenderedPage(path);
        assertThat("component is not visible", component, notNullValue());
        assertThat(component.isEnabled(), is(false));
        return component;
    }

    @SuppressWarnings("unchecked")
    public <T> T assertDisabled(String path, Class<T> componentClass) {
        Component component = assertDisabled(path);
        assertThat(component, is(componentClass));
        return (T) component;
    }

    /**
     * Asserts that a component is visible.
     *
     * @param path path to component in last rendered page
     * @return the visible component
     */
    public Component assertVisible(String path) {
        Component component = tester.getLastRenderedPage().get(path);
        assertThat("component is not visible", component.isVisible(), is(true));
        return component;
    }

    @SuppressWarnings("unchecked")
    public <T> T assertVisible(String path, Class<T> componentClass) {
        Component component = tester.getLastRenderedPage().get(path);
        assertThat(component, is(componentClass));
        return (T) component;
    }

    /**
     * Asserts that a component is not visible.
     *
     * @param path path to component in last rendered page
     * @return the invisible component
     */
    public Component assertInvisible(String path) {
        Component component = tester.getLastRenderedPage().get(path);
        assertThat("component is visible", component.isVisible(), is(false));
        return component;
    }

    /**
     * Returns the extended form tester for the given path.
     *
     * @param path path to form component in last rendered page
     */
    public ExtFormTester form(String path) {
        return new ExtFormTester(tester, path);
    }
}
