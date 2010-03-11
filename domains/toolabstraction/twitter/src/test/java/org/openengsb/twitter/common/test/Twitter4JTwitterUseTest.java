/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.twitter.common.test;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import javax.annotation.Resource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.twitter.common.TwitterConnector;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import twitter4j.Twitter;
import twitter4j.TwitterException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:../test-classes/test-bean.xml" })
public class Twitter4JTwitterUseTest {
    @Resource
    private Twitter twitter;
    @Resource
    private TwitterConnector ourTwitter;
    @Resource
    private String username;

    @Test
    @Ignore
    public void testUpdateStatus() throws TwitterException {
        String s = "test " + new Date();
        ourTwitter.updateStatus(s);
        assertEquals(twitter.getHomeTimeline().get(0).getText(), s);
    }

    @Test
    @Ignore
    public void testSendMessage() throws TwitterException {
        String s = "test " + new Date();
        ourTwitter.sendMessage(username, s);
        assertEquals(twitter.getDirectMessages().get(0).getSender().getScreenName(), username);
        assertEquals(twitter.getDirectMessages().get(0).getText(), s);
    }
}
