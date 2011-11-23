package org.openengsb.core.console.internal.util;

import static org.junit.Assert.assertTrue;
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
        serviceHelper.setWiringService(wiringServiceMock);
    }

    @Test
    public void testGetRunningServices() throws Exception {
        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("  \u001B[1mDummy Name               \u001B[m   Dummy description");
        expectedResult.add("         \u001B[1mid                       \u001B[m   OFFLINE");
        List<String> runningServices = serviceHelper.getRunningServices();
        assertTrue(runningServices.size() > 0);
        for (String s : runningServices) {
            assertTrue(expectedResult.contains(s));
        }
    }
}
