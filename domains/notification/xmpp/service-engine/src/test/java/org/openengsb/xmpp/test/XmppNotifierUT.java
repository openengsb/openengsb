/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.xmpp.test;

import java.util.Date;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.drools.model.Notification;
import org.openengsb.xmpp.XMPPNotifierException;
import org.openengsb.xmpp.XmppNotifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:../test-classes/test-bean.xml" })
public class XmppNotifierUT {

    @Resource
    private XmppNotifier notifier;
    @Resource
    private String recipient;
    @Resource
    private String subject;

    @Test
    public void testSendMessage() throws XMPPNotifierException {
        String s = "test " + new Date();
        Notification notification = new Notification();
        notification.setMessage(s);
        notification.setRecipient(recipient);
        notification.setSubject(subject);
        notification.setAttachments(null);
        this.notifier.notify(notification);
    }
}
