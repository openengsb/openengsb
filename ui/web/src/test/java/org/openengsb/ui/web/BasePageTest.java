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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.injection.annot.test.AnnotApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.context.ContextCurrentService;

public class BasePageTest {

    private WicketTester tester;
    private ContextCurrentService contextService;

    @Before
    public void setup() {
        tester = new WicketTester(new WebApplication() {
            @Override
            public Class<? extends Page> getHomePage() {
                return Index.class;
            }

            @Override
            public Session newSession(Request request, Response response) {
                return new WicketSession(request);
            }
        });
        contextService = mock(ContextCurrentService.class);
        AnnotApplicationContextMock appContext = new AnnotApplicationContextMock();
        appContext.putBean(contextService);
        tester.getApplication().addComponentInstantiationListener(
                new SpringComponentInjector(tester.getApplication(), appContext, false));
        when(contextService.getAvailableContexts()).thenReturn(Arrays.asList(new String[] { "foo", "bar" }));
        tester.startPage(new BasePage());
    }

    @Test
    public void test_default_context_initialization() {
        tester.assertComponent("projectChoice", DropDownChoice.class);
        assertThat("foo", is(WicketSession.get().getThreadContextId()));
    }

    @Test
    public void test_context_change() {
        tester.assertComponent("projectChoice", DropDownChoice.class);
        assertThat("foo", is(WicketSession.get().getThreadContextId()));

        verify(contextService).setThreadLocalContext("foo");

        @SuppressWarnings("unchecked")
        DropDownChoice<String> choice = (DropDownChoice<String>) tester
                .getComponentFromLastRenderedPage("projectChoice");

        choice.setModelObject("bar");
        assertThat("bar", is(WicketSession.get().getThreadContextId()));

        // simulated page reload...
        tester.startPage(new BasePage());
        verify(contextService).setThreadLocalContext("bar");
    }
}
