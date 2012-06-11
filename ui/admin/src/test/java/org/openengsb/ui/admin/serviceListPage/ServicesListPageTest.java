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

package org.openengsb.ui.admin.serviceListPage;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanelTester;
import org.apache.wicket.markup.html.basic.Label;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;
import org.openengsb.ui.admin.AbstractUITest;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

public class ServicesListPageTest extends AbstractUITest {

    @Before
    public void setup() throws Exception {
        Locale.setDefault(new Locale("en"));

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.CONNECTOR_KEY, "bla");

        tester.getApplication().getComponentInstantiationListeners()
            .add(new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));

    }

    private void startPage() {
        tester.startPage(ServiceListPage.class);
        AjaxLazyLoadPanelTester.executeAjaxLazyLoadPanel(tester, tester.getLastRenderedPage());
    }

    @Test
    public void verifyRenderedPage_ShouldBeServiceListPage() {
        startPage();
        tester.assertRenderedPage(ServiceListPage.class);
    }

    @Test
    public void verifyListViews_ShouldBe_Connecting_Online_Disconnecting_And_Disconnected() {
        NullDomainImpl domainService = new NullDomainImpl();
        domainService.setAliveState(AliveState.CONNECTING);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(org.osgi.framework.Constants.SERVICE_PID, "test-service");
        props.put("testprop", "42");
        registerService(domainService, props, NullDomain.class, Domain.class, OpenEngSBService.class);
        startPage();
        Label nameLabel =
            (Label) tester
                .getComponentFromLastRenderedPage("lazy:content:serviceListContainer:serviceListView:0:service.name");
        assertThat(nameLabel.getDefaultModelObjectAsString(), is("test-service"));
        Component stateLabel =
            tester
                .getComponentFromLastRenderedPage("lazy:content:serviceListContainer:serviceListView:0:service.state");
        assertThat(stateLabel.getDefaultModelObjectAsString(), is(AliveState.CONNECTING.name()));
        tester.debugComponentTrees();
    }
}
