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
package org.openengsb.twitter.common.test.unit;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.test.common.Twitter4JTwitterConnector;
import org.openengsb.test.common.TwitterException;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class TestTwitter4JTwitter {
    
    private Twitter twitter;
    
    @BeforeClass
    public void setUp() {
        twitter = new TwitterFactory().getInstance("OpenEngSBTest", "tsetbsgnenepo");
    }
    
    @Test
    public void testTest() throws TwitterException, twitter4j.TwitterException {
        String s = "test" + new Date();
        new Twitter4JTwitterConnector().updateStatus("OpenEngSBTest", "tsetbsgnenepo", s);
        assertEquals(twitter.getHomeTimeline().get(0).getText(), s);
    }
}
