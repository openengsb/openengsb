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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:../test-classes/test-bean.xml"})
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
    public void setup() {
    }

    @After
    public void after() {
    }

    @Ignore
    @Test
    public void simpleUpdateUserStatus() throws FacebookException {
        facebookConnector.updateStatus("TestMessage send on: " + new Date());
        //TODO: Dont know how to test this yet, at the moment you have to check it on your own

    }

    @Test
    public void simpleUpdateUserStatus_retryToCheckIfItIsPossibleToPostASecondTime() throws FacebookException {
        facebookConnector.updateStatus("TestMessage send on1: " + new Date());
        facebookConnector.updateStatus("TestMessage send on2: " + new Date());
    }


}
