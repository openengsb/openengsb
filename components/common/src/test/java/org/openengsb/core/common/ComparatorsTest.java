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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.common.util.Comparators;

public class ComparatorsTest {

    @Test
    public void testDomainProviderComparator_shouldSortDomainProviders() {
        List<DomainProvider> list =
            Arrays.asList(mockDomainProvider("z"), mockDomainProvider("b"), mockDomainProvider("1"));
        Collections.sort(list, Comparators.forDomainProvider());
        assertThat(list.get(0).getId(), is("1"));
        assertThat(list.get(1).getId(), is("b"));
        assertThat(list.get(2).getId(), is("z"));
    }

    private DomainProvider mockDomainProvider(String id) {
        DomainProvider p1 = mock(DomainProvider.class);
        when(p1.getId()).thenReturn(id);
        return p1;
    }

    @Test
    public void testConnectorProviderComparator_shouldSortConnectorProviders() {
        List<ConnectorProvider> list =
            Arrays.asList(mockConnectorProvider("z"), mockConnectorProvider("b"), mockConnectorProvider("1"));
        Collections.sort(list, Comparators.forConnectorProvider());
        assertThat(list.get(0).getId(), is("1"));
        assertThat(list.get(1).getId(), is("b"));
        assertThat(list.get(2).getId(), is("z"));
    }

    private ConnectorProvider mockConnectorProvider(String id) {
        ConnectorProvider p1 = mock(ConnectorProvider.class);
        when(p1.getId()).thenReturn(id);
        return p1;
    }
}
