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
import org.junit.runner.RunWith;
import org.openengsb.facebook.common.FacebookClientImpl;
import org.openengsb.facebook.common.FacebookLogin;
import org.openengsb.facebook.common.FacebookLoginImpl;
import org.openengsb.facebook.common.exceptions.FacebookConnectorException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:../test-classes/test-bean.xml" })
public class FacebookConnectorIT {

    

    @Resource
    private String email;
    @Resource
    private String password;
    @Resource
    private String API_KEY;
    @Resource
    private String SECRET;
    @Resource
    private FacebookConnectorImpl facebookConnector;


    @Before
    public void setup() throws IOException, HttpException, URISyntaxException, FacebookException, FacebookConnectorException {
    }

    @After
    public void after() {
    }

    @Test
    public void simpleUpdateUserStatus() throws FacebookException {
        facebookConnector.updateStatus("test");
        //TODO: Dont know how to test this yet, at the moment you have to check it on your own
       
    }


}
