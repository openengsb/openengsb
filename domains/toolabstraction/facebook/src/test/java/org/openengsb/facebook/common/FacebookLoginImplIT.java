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
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:../test-classes/test-bean.xml" })
public class FacebookLoginImplIT {

    FacebookLogin login;
    @Resource
    private String email;
    @Resource
    private String password;
    @Resource
    private String API_KEY;
    @Resource
    private String SECRET;

    @Before
    public void setup() throws IOException, HttpException, URISyntaxException, FacebookException {
        login = new FacebookLoginImpl();
        
    }

    @After
    public void after() {
        login.closeHttpClient();
    }

    @Test
    public void login_success() throws FacebookException {
        assertTrue(login.loginUser(email, password, API_KEY));
    }


    @Test
    public void login_wrongMail() throws FacebookException {
        assertFalse(login.loginUser("wrongUsername@mail.com", password, API_KEY));
    }


    @Test
    public void login_wrongPass() throws FacebookException {
        assertEquals(false, login.loginUser(email, "wrong password", API_KEY));
    }

    @Test
    public void generateSessionKey() throws FacebookException {
        assertTrue(login.loginUser(email, password, API_KEY));
        assertNotNull(login.createLoggedInFacebookClient(API_KEY, SECRET));
    }
}



