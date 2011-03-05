/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.connector.jira.internal.models.xmlrpc;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Hashtable;

import org.junit.Test;

public class JiraDynamicProxyTest {

    @Test
    public void credentialsArePassed() throws Exception {
        XmlRpcService mockedService = mock(XmlRpcService.class);

        JiraDynamicProxy proxy = new JiraDynamicProxy(mockedService);
        proxy.logIn("aUser", "andHisPassword");

        verify(mockedService).login("aUser", "andHisPassword");
    }

    @Test
    public void correctLoginTokenIsStored() throws Exception {
        XmlRpcService mockedService = mock(XmlRpcService.class);
        when(mockedService.login(anyString(), anyString())).thenReturn("theLoginToken");

        JiraDynamicProxy proxy = new JiraDynamicProxy(mockedService);
        proxy.logIn("aUser", "andHisPassword");
        proxy.logOut();

        verify(mockedService).logout("theLoginToken");
    }

    @Test
    public void creationAttributesArePassed() throws Exception {
        XmlRpcService mockedService = mock(XmlRpcService.class);
        Hashtable<?, ?> mockedAttributes = mock(Hashtable.class);

        JiraDynamicProxy proxy = new JiraDynamicProxy(mockedService);
        proxy.createIssue(mockedAttributes);

        verify(mockedService).createIssue(anyString(), same(mockedAttributes));
    }

    @Test
    public void updateArgumentsArePassed() throws Exception {
        XmlRpcService mockedService = mock(XmlRpcService.class);
        Hashtable<?, ?> mockedAttributes = mock(Hashtable.class);

        JiraDynamicProxy proxy = new JiraDynamicProxy(mockedService);
        proxy.updateIssue("anIssueKey", mockedAttributes);

        verify(mockedService).updateIssue(anyString(), eq("anIssueKey"), same(mockedAttributes));
    }

    @Test
    public void addCommentArgumentsArePassed() throws Exception {
        XmlRpcService mockedService = mock(XmlRpcService.class);

        JiraDynamicProxy proxy = new JiraDynamicProxy(mockedService);
        proxy.addComment("anIssueKey", "aComment");

        verify(mockedService).addComment(anyString(), eq("anIssueKey"), eq("aComment"));
    }

}
