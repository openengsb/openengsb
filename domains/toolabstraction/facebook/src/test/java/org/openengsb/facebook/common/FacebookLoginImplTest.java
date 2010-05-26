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
import org.apache.http.HttpException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.facebook.common.exceptions.FacebookConnectorException;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class FacebookLoginImplTest {

    FacebookLogin login;
    final String email = "azugem@gmail.com";
    final String password = "schwer2?";

    final String API_KEY = "87e992543f887369febb9d056c14f145";
    final String SECRET = "43438a4fed1b5fed6f7bf6f1c3e5f87f";

    @Before
    public void setup() throws IOException, HttpException, URISyntaxException, FacebookConnectorException {
        login = new FacebookLoginImpl();
        login.initHttpClient();
    }

    @After
    public void after(){
         login.closeHttpClient();
    }

    @Test
    public void testLogin_success() throws FacebookConnectorException {
        assertTrue(login.loginUser(email,password, API_KEY));
    }
    

    @Test
    public void testLogin_wrongMail() throws FacebookConnectorException {
        assertFalse(login.loginUser("wrongUsername@mail.com",password, API_KEY));
    }


    @Test
    public void testLogin_wrongPass() throws FacebookConnectorException {
        assertEquals(false, login.loginUser(email, "wrong password", API_KEY));
    }

    @Test
    public void testGenerateSessionKey() throws FacebookConnectorException, FacebookException {
        assertTrue(login.loginUser(email,password, API_KEY));
        assertNotNull(login.createLoggedInFacebookClient(API_KEY, SECRET));
    }
}



