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

package org.openengsb.core.common;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.DomainEvents;
import org.openengsb.core.api.Event;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.openengsb.core.test.BundleStringHelper;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullEvent;
import org.osgi.framework.BundleContext;

public class AbstractDomainProviderTest extends AbstractOpenEngSBTest {

    private interface NullDomainEvents extends DomainEvents {

        void raiseEvent(NullEvent event);

        void raiseEvent();

        void raiseEvent(String string);

        void someMethod();
    }

    private static class DummyProvider extends AbstractDomainProvider<NullDomain, NullDomainEvents> {

    }

    private AbstractDomainProvider<NullDomain, NullDomainEvents> provider;
    private BundleContext bundleContext;

    @Before
    public void setup() {
        provider = new DummyProvider();
        provider.setId(NullDomain.class.getSimpleName());
        bundleContext = BundleStringHelper.createBundleContextMockWithBundleStrings();
        provider.setBundleContext(bundleContext);
    }

    @Test
    public void testGetName_shouldLookupInDomainName() throws Exception {
        assertThat(provider.getName().getString(Locale.getDefault()), is("name"));
    }

    @Test
    public void testGetDescription_shouldLookupInDomainDescription() throws Exception {
        assertThat(provider.getDescription().getString(Locale.getDefault()), is("desc"));
    }

    @Test
    public void testParameterizedDomain_shouldExtractDomainInterfaceFromGenerics() throws Exception {
        Assert.assertEquals(NullDomain.class, provider.getDomainInterface());
    }

    @Test
    public void testGetId_shouldReturnSimpleClassNameOfDomain() throws Exception {
        assertThat(provider.getId(), is(NullDomain.class.getSimpleName()));
    }

    @Test
    public void testParameterizedDomainEvents_shouldExtractDomainEventsInterfaceFromGenerics() throws Exception {
        Assert.assertEquals(NullDomainEvents.class, provider.getDomainEventInterface());
    }

    @Test
    public void testGetEvents_shouldReturnDummyEvent() throws Exception {
        List<Class<? extends Event>> events = provider.getEvents();
        assertThat(events.contains(NullEvent.class), is(true));
        assertThat(events.size(), is(1));
    }
}
