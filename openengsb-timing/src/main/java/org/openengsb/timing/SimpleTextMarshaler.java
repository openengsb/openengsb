/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.timing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.quartz.support.DefaultQuartzMarshaler;
import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SimpleTextMarshaler extends DefaultQuartzMarshaler {

    private int i = 0;

    @Override
    public void populateNormalizedMessage(NormalizedMessage message, JobExecutionContext context)
            throws JobExecutionException, MessagingException {
        super.populateNormalizedMessage(message, context);
        message.setProperty("contextId", "42");

        try {
            String xml;
            if (i++ % 2 == 0) {
                message.setProperty("messageType", "context/store");

                List<Segment> list = new ArrayList<Segment>();
                list.add(new TextSegment.Builder(i + "/foo").text(UUID.randomUUID().toString()).build());
                list.add(new TextSegment.Builder(i + "/bar").text(UUID.randomUUID().toString()).build());
                list.add(new TextSegment.Builder(i + "/buz").text(UUID.randomUUID().toString()).build());

                ListSegment listSegment = new ListSegment.Builder("/").list(list).build();
                xml = listSegment.toXML();
            } else {
                message.setProperty("messageType", "context/request");
                TextSegment text = new TextSegment.Builder("path").text("/").build();
                xml = text.toXML();
            }

            message.setContent(new StringSource(xml));
        } catch (Exception e) {
            throw new RuntimeException(e); // TODO
        }
    }
}
