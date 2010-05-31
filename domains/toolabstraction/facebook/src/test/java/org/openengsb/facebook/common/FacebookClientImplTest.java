/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/
package org.openengsb.facebook.common;

import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.FacebookJaxbRestClient;
import org.apache.http.HttpException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.facebook.common.FacebookClient;
import org.openengsb.facebook.common.FacebookClientImpl;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;



public class FacebookClientImplTest {
    FacebookJaxbRestClient facebookJaxbClientMock;
    FacebookClient fbClient;



    @Before
    public void setup() throws IOException, HttpException, URISyntaxException {
        facebookJaxbClientMock = Mockito.mock(FacebookJaxbRestClient.class);
        fbClient = new FacebookClientImpl(facebookJaxbClientMock);
    }

    @After
    public void after(){

    }

    @Test
    public void puplishToWallNoErrorShouldBeReturned() throws FacebookException {
        Mockito.when(facebookJaxbClientMock.users_setStatus(Mockito.anyString())).thenReturn(true);
        fbClient.updateStatus("test");
        Mockito.verify(facebookJaxbClientMock,Mockito.times(1)).users_setStatus(Mockito.eq("test"));
    }

    @Test
    public void getLoggedInUserID() throws FacebookException {
        Mockito.when(facebookJaxbClientMock.users_getLoggedInUser()).thenReturn(new Long(1234));
        fbClient.getLoggedInUserID();
        Mockito.verify(facebookJaxbClientMock,Mockito.times(1)).users_getLoggedInUser();
    }

}