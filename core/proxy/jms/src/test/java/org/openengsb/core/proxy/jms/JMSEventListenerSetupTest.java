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

package org.openengsb.core.proxy.jms;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.service.DomainService;
import org.springframework.jms.listener.AbstractMessageListenerContainer;

public class JMSEventListenerSetupTest {

    private static final String ID = "ID";
    private DomainService domainService;
    private DomainProvider provider;

    @Before
    public void setUp() {
        domainService = mock(DomainService.class);
        provider = Mockito.mock(DomainProvider.class);
        when(provider.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) {
                return TestInterface.class;
            }
        });
        when(provider.getId()).thenReturn(ID);
        when(domainService.domains()).thenReturn(Arrays.asList(new DomainProvider[]{provider}));
    }

    @Test
    public void callAddEventListeners_ShouldSetEventListenersCorrectly() {
        when(domainService.domains()).thenReturn(Arrays.asList(new DomainProvider[]{provider, provider}));
        ConnectionFactory mock2 = mock(ConnectionFactory.class);

        MessageListenerContainerFactory messageListenerFactoryMock = mock(MessageListenerContainerFactory.class);
        AbstractMessageListenerContainer messageListenerMock = mock(AbstractMessageListenerContainer.class);
        when(messageListenerFactoryMock.instance()).thenReturn(messageListenerMock);
        MessageListenerFactory messageListenerFactory = mock(MessageListenerFactory.class);
        MessageListener messageListener = mock(MessageListener.class);
        when(messageListenerFactory.instance(Mockito.any(DomainProvider.class))).thenReturn(messageListener);

        new JMSEventListenerSetup(domainService, messageListenerFactoryMock, mock2, messageListenerFactory);

        verify(messageListenerFactoryMock, times(2)).instance();
        verify(messageListenerMock, times(2)).setConnectionFactory(mock2);
        verify(messageListenerMock, times(2)).setDestinationName("ID_event_send");
        verify(messageListenerMock, times(2)).setMessageListener(messageListener);
        verify(messageListenerMock, times(2)).start();
    }

    private static interface TestInterface extends Domain {
        void log(int i);
    }
}
