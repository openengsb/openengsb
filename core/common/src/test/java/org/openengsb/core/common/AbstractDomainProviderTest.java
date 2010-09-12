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
package org.openengsb.core.common;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.util.BundleStringsTest;
import org.osgi.framework.BundleContext;

public class AbstractDomainProviderTest {

    private static interface DummyDomain extends Domain {

    }

    private static class DummyProvider extends AbstractDomainProvider<DummyDomain> {

        @Override
        public String getId() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Class<? extends Event>> getEvents() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException();
        }

    }

    private AbstractDomainProvider<DummyDomain> provider;
    private BundleContext bundleContext;

    @Before
    public void setup() {
        provider = new DummyProvider();
        bundleContext = BundleStringsTest.createBundleContextMockWithBundleStrings();
        provider.setBundleContext(bundleContext);
    }

    @Test
    public void getName_shouldLookupInDomainName() {
        assertThat(provider.getName(), is("name"));
    }

    @Test
    public void getDescription_shouldLookupInDomainDescription() {
        assertThat(provider.getDescription(), is("desc"));
    }

    @Test
    public void parameterizedDomain_shouldExtractDomainInterfaceFromGenerics() {
        Assert.assertEquals(DummyDomain.class, provider.getDomainInterface());
    }
}
