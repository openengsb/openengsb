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

package org.openengsb.twitter.test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.util.FileUpload;
import org.openengsb.drools.model.Attachment;
import org.openengsb.drools.model.Notification;
import org.openengsb.twitter.TwitterNotifier;
import org.openengsb.twitter.common.Twitter4JTwitterConnector;
import org.openengsb.twitter.common.util.UrlShortenerUtil;
import org.openengsb.twitter.common.util.ZipUtil;

public class TwitterNotifierUnitTest {
    private Twitter4JTwitterConnector twitter;
    private FileUpload fileUpload;
    private ZipUtil zipUtil;
    private UrlShortenerUtil urlShortener;
    private TwitterNotifier notifier;

    @Before
    public void setUp() throws IOException {
        twitter = Mockito.mock(Twitter4JTwitterConnector.class);
        fileUpload = Mockito.mock(FileUpload.class);
        zipUtil = Mockito.mock(ZipUtil.class);
        urlShortener = Mockito.mock(UrlShortenerUtil.class);

        notifier = new TwitterNotifier();
        notifier.setTwitterCon(twitter);
        notifier.setFileUpload(fileUpload);
        notifier.setZipUtil(zipUtil);
        notifier.setUrlShortener(urlShortener);

        setUpMocking();
    }

    @Test
    public void testUpdateStatusWithoutAttachments() throws IOException {
        Notification n = new Notification();
        n.setMessage("testmessage");
        notifier.notify(n);

        Mockito.verify(twitter, Mockito.times(1)).updateStatus(Mockito.eq("testmessage"));
        checkAttachment(false);
    }

    @Test
    public void testSendMessageWithoutAttachments() throws IOException {
        Notification n = new Notification();
        n.setMessage("testmessage");
        n.setRecipient("test");
        notifier.notify(n);

        Mockito.verify(twitter, Mockito.times(1)).sendMessage(Mockito.eq("test"), Mockito.eq("testmessage"));
        checkAttachment(false);
    }

    @Test
    public void testUpdateStatusWithAttachments() throws IOException {
        Notification n = new Notification();
        n.setMessage("testmessage");
        n.setAttachments(prepareAttachments());
        notifier.notify(n);

        Mockito.verify(twitter, Mockito.times(1)).updateStatus(Mockito.eq("Attachment: testShortURL\ntestmessage"));
        checkAttachment(true);
    }

    @Test
    public void testSendMessageWithAttachments() throws IOException {
        Notification n = new Notification();
        n.setMessage("testmessage");
        n.setRecipient("test");
        n.setAttachments(prepareAttachments());
        notifier.notify(n);

        Mockito.verify(twitter, Mockito.times(1)).sendMessage(Mockito.eq("test"),
                Mockito.eq("Attachment: testShortURL\ntestmessage"));
        checkAttachment(true);
    }

    @Test
    public void testAttachmentErrorCatching() throws IOException {
        Mockito.when(urlShortener.getTinyUrl(Mockito.any(URL.class))).thenThrow(new IOException());

        Notification n = new Notification();
        n.setMessage("testmessage");
        n.setAttachments(prepareAttachments());
        notifier.notify(n);

        Mockito.verify(twitter, Mockito.times(1)).updateStatus(Mockito.eq("testmessage"));
        checkAttachment(true);
    }

    private void setUpMocking() throws IOException {
        Mockito.when(zipUtil.zipAttachments(Mockito.any(Attachment[].class))).thenReturn(new byte[] { 1, 2, 3, 4, 5 });
        Mockito.when(fileUpload.uploadFile(Mockito.any(byte[].class), Mockito.anyString())).thenReturn(
                new URL("ftp://testURL"));
        Mockito.when(urlShortener.getTinyUrl(Mockito.any(URL.class))).thenReturn("testShortURL");
    }

    private List<Attachment> prepareAttachments() {
        List<Attachment> as = new ArrayList<Attachment>();
        as.add(new Attachment(new byte[] { 1, 2, 3 }, "test", "test1"));
        as.add(new Attachment(new byte[] { 2, 3, 4 }, "test", "test2"));
        as.add(new Attachment(new byte[] { 4, 5, 6 }, "test", "test3"));

        return as;
    }

    private void checkAttachment(boolean attachments) throws IOException {
        Mockito.verify(zipUtil, Mockito.times(attachments ? 1 : 0)).zipAttachments(Mockito.any(Attachment[].class));
        Mockito.verify(fileUpload, Mockito.times(attachments ? 1 : 0)).uploadFile(Mockito.any(byte[].class),
                Mockito.anyString());
        Mockito.verify(urlShortener, Mockito.times(attachments ? 1 : 0)).getTinyUrl(Mockito.any(URL.class));
    }
}
