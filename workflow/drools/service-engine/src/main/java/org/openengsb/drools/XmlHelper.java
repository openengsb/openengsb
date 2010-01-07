/**

 Copyright 2010 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.drools;

import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.openengsb.core.model.Event;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

/**
 * Helper-methods for parsing Xml-messages.
 */
public final class XmlHelper {

    /**
     * hidden default-constructor.
     */
    private XmlHelper() {
    }

    /**
     * Transformer used for transforming the Source into a parsable
     * xml-document-object.
     */
    private static SourceTransformer t = new SourceTransformer();

    /**
     * parse an Event-message.
     * 
     * @param msg message that should be parsed.
     * @return the parsed Event-Object.
     */
    public static Event parseEvent(NormalizedMessage msg) {
        try {
            String xml = t.toString(msg.getContent());
            return Transformer.toEvent(xml);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

}
