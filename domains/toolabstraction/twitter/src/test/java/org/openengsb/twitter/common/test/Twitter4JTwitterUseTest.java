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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.httpclient.HttpException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.drools.model.Attachment;
import org.openengsb.twitter.common.TwitterConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

<<<<<<< HEAD:domains/toolabstraction/twitter/src/test/java/org/openengsb/twitter/common/test/unit/TestTwitter4JTwitter.java
public class TestTwitter4JTwitter {
<<<<<<< HEAD
    
    private Twitter twitter;
    
=======
=======
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:../test-classes/test-bean.xml" })
public class Twitter4JTwitterUseTest {
>>>>>>> Refactoring and correction due to comments:domains/toolabstraction/twitter/src/test/java/org/openengsb/twitter/common/test/Twitter4JTwitterUseTest.java

    private static Twitter twitter;
    private TwitterConnector ourTwitter;

    private static final String USERNAME = "OpenEngSBTest";
    private static final String PASSWORD = "tsetbsgnenepo";

    @Autowired
    public void setOurTwitter(TwitterConnector ourTwitter) {
        this.ourTwitter = ourTwitter;
    }

>>>>>>> create and test zip-file of attachments, create and test tinyurl, update configuration
    @BeforeClass
<<<<<<< HEAD:domains/toolabstraction/twitter/src/test/java/org/openengsb/twitter/common/test/unit/TestTwitter4JTwitter.java
    public void setUp() {
        twitter = new TwitterFactory().getInstance("OpenEngSBTest", "tsetbsgnenepo");
=======
    public static void setUp() {
        twitter = new TwitterFactory().getInstance(USERNAME, PASSWORD);
>>>>>>> Refactoring and correction due to comments:domains/toolabstraction/twitter/src/test/java/org/openengsb/twitter/common/test/Twitter4JTwitterUseTest.java
    }
    

    @Test
<<<<<<< HEAD
    public void testTest() throws TwitterException, twitter4j.TwitterException {
        String s = "test" + new Date();
        new Twitter4JTwitterConnector().updateStatus("OpenEngSBTest", "tsetbsgnenepo", s);
        assertEquals(twitter.getHomeTimeline().get(0).getText(), s);
    }
=======
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
        ourTwitter.sendMessage(USERNAME, s);
        assertEquals(twitter.getDirectMessages().get(0).getSender().getScreenName(), USERNAME);
        assertEquals(twitter.getDirectMessages().get(0).getText(), s);
    }

    @Test
    public void testZipAttachments() throws IOException {
        String[] files = new String[] { "testfile1.jpg", "testfile2.jpg", "testfile3.jpg" };
        Attachment[] attachments = new Attachment[files.length];
        for (int i = 0; i < files.length; i++) {
            File src = new File("target\\test-classes\\" + files[i]);
            FileInputStream fileInputStream = new FileInputStream(src);
            byte[] data = new byte[(int) src.length()];
            fileInputStream.read(data);
            fileInputStream.close();
            attachments[i] = new Attachment(data, "image", files[i]);
        }

        ourTwitter.zipAttachments(attachments, "target\\test-classes\\testarchive.zip");

        File zip = new File("target\\test-classes\\testarchive.zip");
        assertTrue(zip.exists());
        assertTrue(zip.length() > 0);
    }

    @Test
    public void testTinyUrl() throws HttpException, IOException {
        String s = "http://maps.google.at/maps/place?cid=2469784843158832493&q=tu+wien&hl=de&cd=1&cad=src:pplink&ei=yKOPS-jIA4mH_Qb5pPA7";
        String tiny = ourTwitter.getTinyUrl(s);
        assertNotNull(tiny);
        assertTrue(s.length() > tiny.length());
    }
>>>>>>> create and test zip-file of attachments, create and test tinyurl, update configuration
}
