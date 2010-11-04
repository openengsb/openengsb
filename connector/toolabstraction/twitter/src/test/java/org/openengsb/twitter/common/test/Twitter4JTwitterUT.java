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

package org.openengsb.twitter.common.test;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.twitter.common.TwitterConnector;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import twitter4j.Twitter;
import twitter4j.TwitterException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:../test-classes/test-bean.xml" })
public class Twitter4JTwitterUT {
    @Resource
    private Twitter twitter;
    @Resource
    private TwitterConnector ourTwitter;
    @Resource
    private String username;

    @Test
    public void testUpdateStatus() throws TwitterException {
        String s = "test " + new Date();
        this.ourTwitter.updateStatus(s);
        assertEquals(this.twitter.getHomeTimeline().get(0).getText(), s);
    }

    @Test
    public void testSendMessage() throws TwitterException {
        String s = "test " + new Date();
        this.ourTwitter.sendMessage(this.username, s);
        assertEquals(this.twitter.getDirectMessages().get(0).getSender().getScreenName(), this.username);
        assertEquals(this.twitter.getDirectMessages().get(0).getText(), s);
    }
}
