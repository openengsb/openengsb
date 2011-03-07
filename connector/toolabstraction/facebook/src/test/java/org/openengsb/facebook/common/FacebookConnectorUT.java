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

package org.openengsb.facebook.common;

import com.google.code.facebookapi.FacebookException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-bean.xml"})
public class FacebookConnectorUT {


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

    @Test
    public void simpleUpdateUserStatus() throws FacebookException, IOException {
        facebookConnector.updateMessageNoErrorHandling("TestMessage send on: " + new Date());
    }

    @Test
    public void simpleUpdateUserStatusPostTwoMessages() throws FacebookException, IOException {
        facebookConnector.updateMessageNoErrorHandling("TestMessage 1 send on: " + new Date());
        facebookConnector.updateMessageNoErrorHandling("TestMessage 2 send on: " + new Date());
    }

    @Test
    public void updateUserMessageTruncateMessage() throws FacebookException, IOException {
        facebookConnector.updateMessageNoErrorHandling("" +
                "Who rides, so late, through night and wind?" +
                "It is the father with his child." +
                "He has the boy well in his arm," +
                "He holds him safely, he keeps him warm." +

                "\"My son, why do you hide your face so anxiously?" +
                "\"Father, do you not see the Erl king?" +
                "The Erl king with crown and tail?" +
                "\"My son, it's a wisp of fog." +

                "\"You lovely child, come, go with me!\n" +
                "Many a beautiful game I'll play with you;" +
                "Many colourful flowers are on the shore," +
                "My mother has many golden robes." +

                "\"My father, my father, and don't you hear" +
                "What Erl king is quietly promising me?" +
                "\"Be calm, stay calm, my child;" +
                "The wind is rustling through withered leaves." +

                "\"Do you want to come with me, dear boy?" +
                "My daughters shall wait on you fine;" +
                "My daughters will lead the nightly dance," +
                "And rock and dance and sing you to sleep." +

                "\"My father, my father, and don't you see there" +
                "Erl king's daughters in the gloomy place?" +
                "\"My son, my son, I see it clearly:" +
                "The old willows they shimmer so grey." +

                "\"I love you, your beautiful form entices me;" +
                "And if you're not willing, I shall use force." +
                "\"My father, my father, he's grabbing me now!" +
                "Erl king has done me some harm!" +

                "The father shudders; he swiftly rides on," +
                "He holds the moaning child in his arms," +
                "is hardly able to reach his farm;" +
                "In his arms, the child was dead.");
    }

}
