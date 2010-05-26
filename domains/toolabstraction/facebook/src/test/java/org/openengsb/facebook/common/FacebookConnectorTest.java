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
import org.junit.Ignore;
import org.junit.Test;
import org.openengsb.facebook.common.FacebookClientImpl;
import org.openengsb.facebook.common.FacebookLogin;
import org.openengsb.facebook.common.FacebookLoginImpl;
import org.openengsb.facebook.common.exceptions.FacebookConnectorException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: Philipp H
 * Date: 19.05.2010
 * Time: 16:15:04
 * Mail: e0725710@student.tuwien.ac.at
 */
public class FacebookConnectorTest {

    
    final String email = "azugem@gmail.com";
    final String password = "password?";
    final String API_KEY = "87e992543f887369febb9d056c14f145";
    final String SECRET = "43438a4fed1b5fed6f7bf6f1c3e5f87f";
    private FacebookConnectorImpl facebookConnector;


    @Before
    public void setup() throws IOException, HttpException, URISyntaxException, FacebookException, FacebookConnectorException {
        facebookConnector = new FacebookConnectorImpl(email, password, API_KEY, SECRET);
    }

    @After
    public void after() {

    }


    @Test
    public void testSimpleUpdateStatus() throws FacebookException {
        facebookConnector.updateStatus("test");
        
       // assertTrue("100001136838040".equals(result.get("actor_id")));
       // System.out.println(result.get("message"));
    }


}
