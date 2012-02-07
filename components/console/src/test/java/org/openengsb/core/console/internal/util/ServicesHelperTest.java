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

package org.openengsb.core.console.internal.util;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.l10n.LocalizableString;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;

public class ServicesHelperTest {

    private ServicesHelper serviceHelper;

    @Before
    public void init() {
        DefaultOsgiUtilsService osgiServiceMock = mock(DefaultOsgiUtilsService.class);
        final List<DomainProvider> domainProviders = new ArrayList<DomainProvider>();
        final List<Domain> domainEndpoints = new ArrayList<Domain>();
        Domain domainEndpoint = new NullDomainImpl("id");
        domainEndpoints.add(domainEndpoint);
        DomainProvider domainProviderMock = mock(DomainProvider.class);
        when(domainProviderMock.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return NullDomain.class;
            }
        });
        LocalizableString descriptionMock = mock(LocalizableString.class);
        LocalizableString nameDescritptionMock = mock(LocalizableString.class);
        when(descriptionMock.getString(any(Locale.class))).thenReturn("Dummy description");
        when(nameDescritptionMock.getString(any(Locale.class))).thenReturn("Dummy Name");
        when(domainProviderMock.getDescription()).thenReturn(descriptionMock);
        when(domainProviderMock.getName()).thenReturn(nameDescritptionMock);
        domainProviders.add(domainProviderMock);

        when(osgiServiceMock.listServices(DomainProvider.class)).thenAnswer(new Answer<List<DomainProvider>>() {
            @Override
            public List<DomainProvider> answer(InvocationOnMock invocationOnMock) throws Throwable {

                return domainProviders;
            }
        });
        WiringService wiringServiceMock = mock(WiringService.class);
        when(osgiServiceMock.getService(WiringService.class)).thenReturn(wiringServiceMock);
        when(wiringServiceMock.getDomainEndpoints(NullDomain.class, "*")).thenAnswer(new Answer<List<? extends
            Domain>>() {
            @Override
            public List<? extends Domain> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return domainEndpoints;
            }
        });

        serviceHelper = new ServicesHelper();
        serviceHelper.setOsgiUtilsService(osgiServiceMock);
    }


}
