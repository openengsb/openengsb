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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.twitter.common.Twitter4JTwitterConnector;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class Twitter4JTwitterTest {
    private Twitter4JTwitterConnector ourTwitter;
    private Twitter twitter;

    @Before
    public void setUp() {
        twitter = Mockito.mock(Twitter.class);
        ourTwitter = new Twitter4JTwitterConnector();
        ourTwitter.setTwitter(twitter);
    }

    @Test
    public void testStatusErrorCatching() throws TwitterException {
        Mockito.when(twitter.updateStatus(Mockito.anyString())).thenThrow(new TwitterException("test"));
        ourTwitter.updateStatus("test");
    }

    @Test
    public void testMessageErrorCatching() throws TwitterException {
        Mockito.when(twitter.sendDirectMessage(Mockito.anyString(), Mockito.anyString())).thenThrow(new TwitterException("test"));
        ourTwitter.sendMessage("test", "test");
    }

    @Test
    public void testUpdateStatus() throws TwitterException {
        ourTwitter.updateStatus("test");
        Mockito.verify(twitter, Mockito.times(1)).updateStatus(Mockito.eq("test"));
    }

    @Test
    public void testSendMessage() throws TwitterException {
        ourTwitter.sendMessage("test", "test");
        Mockito.verify(twitter, Mockito.times(1)).sendDirectMessage(Mockito.eq("test"), Mockito.eq("test"));
    }
}
