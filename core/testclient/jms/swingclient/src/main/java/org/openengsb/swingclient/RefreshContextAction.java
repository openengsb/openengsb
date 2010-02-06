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
package org.openengsb.swingclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.jms.JMSException;

import org.openengsb.contextcommon.Context;
import org.openengsb.contextcommon.ContextSegmentTransformer;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;
import org.openengsb.util.serialization.SerializationException;

public class RefreshContextAction implements ActionListener {

    private ContextPanel panel;

    public RefreshContextAction(ContextPanel panel) {
        this.panel = panel;

    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        try {
            String result = OpenEngSBClient.contextCall("request", getMessage());
            Segment segment = Segment.fromXML(result);
            Context context = ContextSegmentTransformer.toContext(segment);

            panel.tree.updateTree(context);

        } catch (JMSException e) {
            throw new RuntimeException(e);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    private String getMessage() {
        try {
            TextSegment text = new TextSegment.Builder("path").text("/").build();
            String xml = text.toXML();
            return xml;
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }
}
