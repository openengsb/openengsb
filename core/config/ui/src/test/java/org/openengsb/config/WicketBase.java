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

import java.util.List;

import org.apache.wicket.spring.injection.annot.test.AnnotApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.mockito.Mockito;
import org.openengsb.config.dao.EndpointDao;
import org.openengsb.config.dao.ServiceAssemblyDao;
import org.openengsb.config.editor.ContextStringResourceLoader;
import org.openengsb.config.jbi.ComponentParser;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.service.AssemblyService;
import org.openengsb.config.service.ComponentService;
import org.openengsb.config.service.ContextService;

import com.google.common.collect.Lists;

public class WicketBase {
    protected List<ComponentType> components;
    protected ComponentService mockedComponentService;
    protected AssemblyService mockedAssemblyService;
    protected WicketTester tester;
    protected ExtWicketTester extTester;

    @Before
    public void setup() {
        components = ComponentParser.parseComponents(Lists.newArrayList(ClassLoader
                .getSystemResourceAsStream("test-connector.xml")));
        mockedComponentService = Mockito.mock(ComponentService.class);
        Mockito.when(mockedComponentService.getComponents()).thenReturn(components);
        Mockito.when(mockedComponentService.getComponent("test-connector")).thenReturn(components.get(0));
        Mockito.when(mockedComponentService.getEndpoints()).thenReturn(components.get(0).getEndpoints());
        mockedAssemblyService = Mockito.mock(AssemblyService.class);
        final AnnotApplicationContextMock ctx = new AnnotApplicationContextMock();
        ctx.putBean(mockedComponentService);
        ctx.putBean(mockedAssemblyService);
        ctx.putBean(Mockito.mock(ContextService.class));
        ctx.putBean(Mockito.mock(ServiceAssemblyDao.class));
        ctx.putBean(Mockito.mock(EndpointDao.class));

        ContextStringResourceLoader.instance.reset();
        ContextStringResourceLoader.instance.addResourceFiles("test-connector", ClassLoader
                .getSystemResourceAsStream("test-connector.properties"));

        ConfigApplication app = new ConfigApplication();
        app.setApplicationContext(ctx);
        tester = new WicketTester(app);
        extTester = new ExtWicketTester(tester);
    }
}
