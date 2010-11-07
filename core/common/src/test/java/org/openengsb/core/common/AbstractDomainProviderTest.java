/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.openengsb.core.common.l10n.BundleStringsTest;
import org.openengsb.core.common.support.NullDomain;
import org.openengsb.core.common.support.NullEvent;
import org.osgi.framework.BundleContext;

public class AbstractDomainProviderTest {

    private static interface NullDomainEvents extends DomainEvents {

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
        bundleContext = BundleStringsTest.createBundleContextMockWithBundleStrings();
        provider.setBundleContext(bundleContext);
    }

    @Test
    public void getName_shouldLookupInDomainName() {
        assertThat(provider.getName().getString(Locale.getDefault()), is("name"));
    }

    @Test
    public void getDescription_shouldLookupInDomainDescription() {
        assertThat(provider.getDescription().getString(Locale.getDefault()), is("desc"));
    }

    @Test
    public void parameterizedDomain_shouldExtractDomainInterfaceFromGenerics() {
        Assert.assertEquals(NullDomain.class, provider.getDomainInterface());
    }

    @Test
    public void getId_shouldReturnSimpleClassNameOfDomain() {
        assertThat(provider.getId(), is(NullDomain.class.getSimpleName()));
    }

    @Test
    public void parameterizedDomainEvents_shouldExtractDomainEventsInterfaceFromGenerics() {
        Assert.assertEquals(NullDomainEvents.class, provider.getDomainEventInterface());
    }

    @Test
    public void getEvents_shouldReturnDummyEvent() {
        List<Class<? extends Event>> events = provider.getEvents();
        assertThat(events.contains(NullEvent.class), is(true));
        assertThat(events.size(), is(1));
    }
}
