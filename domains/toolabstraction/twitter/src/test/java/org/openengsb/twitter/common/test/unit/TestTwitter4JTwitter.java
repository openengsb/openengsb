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
import org.openengsb.drools.model.Attachment;
import org.openengsb.test.common.Twitter4JTwitterConnector;
import org.openengsb.test.common.TwitterException;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class TestTwitter4JTwitter {
<<<<<<< HEAD
    
    private Twitter twitter;
    
=======

    private static Twitter twitter;

>>>>>>> create and test zip-file of attachments, create and test tinyurl, update configuration
    @BeforeClass
    public void setUp() {
        twitter = new TwitterFactory().getInstance("OpenEngSBTest", "tsetbsgnenepo");
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
    public void testUpdateStatus() throws TwitterException, twitter4j.TwitterException {
        String s = "test " + new Date();
        new Twitter4JTwitterConnector().updateStatus("OpenEngSBTest", "tsetbsgnenepo", s);
        assertEquals(twitter.getHomeTimeline().get(0).getText(), s);
    }

    @Test
    @Ignore
    public void testSendMessage() throws TwitterException, twitter4j.TwitterException {
        String s = "test " + new Date();
        new Twitter4JTwitterConnector().sendMessage("OpenEngSBTest", "tsetbsgnenepo", "OpenEngSBTest", s);
        assertEquals(twitter.getDirectMessages().get(0).getSender().getScreenName(), "OpenEngSBTest");
        assertEquals(twitter.getDirectMessages().get(0).getText(), s);
    }

    @Test
    public void testZipAttachments() throws IOException {   
        String[] files = new String[] {"testfile1.jpg", "testfile2.jpg", "testfile3.jpg"};
        Attachment[] attachments = new Attachment[files.length];
        for(int i = 0; i < files.length; i++)
        {
            File src = new File("target\\test-classes\\" + files[i]);
            FileInputStream fileInputStream = new FileInputStream(src);
            byte[] data = new byte[(int) src.length()];
            fileInputStream.read(data);
            fileInputStream.close();
            attachments[i] = new Attachment(data, "image", files[i]);
        }

        Twitter4JTwitterConnector.zipAttachments(attachments, "target\\test-classes\\testarchive.zip");
        
        File zip = new File("target\\test-classes\\testarchive.zip");
        assertTrue(zip.exists());
        assertTrue(zip.length() > 0);
    }
    
    @Test
    public void testTinyUrl() throws HttpException, IOException {
        String s = "http://maps.google.at/maps/place?cid=2469784843158832493&q=tu+wien&hl=de&cd=1&cad=src:pplink&ei=yKOPS-jIA4mH_Qb5pPA7";
        String tiny = Twitter4JTwitterConnector.getTinyUrl(s);
        assertNotNull(tiny);
        assertTrue(s.length() > tiny.length());
    }
>>>>>>> create and test zip-file of attachments, create and test tinyurl, update configuration
}
