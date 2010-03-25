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
package org.openengsb.contextcommon;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.xmlmapping.XMLContext;
import org.openengsb.core.xmlmapping.XMLContextEntry;
import org.openengsb.util.serialization.JibxXmlSerializer;
import org.openengsb.util.serialization.SerializationException;

public class ContextTransformer {

    private final static JibxXmlSerializer serializer = new JibxXmlSerializer();

    public static String toXml(Context ctx) {
        XMLContext xmlContext = toXmlContext(ctx);
        try {
            StringWriter writer = new StringWriter();
            serializer.serialize(xmlContext, writer);
            return writer.toString();
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Context fromXml(String xml) {
        try {
            StringReader reader = new StringReader(xml);
            XMLContext xmlContext = serializer.deserialize(XMLContext.class, reader);
            return toContext(xmlContext);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    public static XMLContext toXmlContext(Context ctx) {
        XMLContext xmlContext = new XMLContext();
        List<XMLContextEntry> entries = new ArrayList<XMLContextEntry>();
        toXmlContext(entries, "/", ctx);
        xmlContext.setEntries(entries);
        return xmlContext;
    }

    public static Context toContext(XMLContext xmlContext) {
        ContextStore store = new ContextStore();
        toContext(store, xmlContext);
        return store.getContext("/");
    }

    private static void toContext(ContextStore store, XMLContext segment) {
        for (XMLContextEntry entry : segment.getEntries()) {
            String key = entry.getKey();
            String value = entry.getValue();
            store.setValue(key, value);
        }
    }

    private static void toXmlContext(List<XMLContextEntry> entries, String path, Context ctx) {
        for (String key : ctx.getKeys()) {
            XMLContextEntry xmlContextEntry = new XMLContextEntry();
            xmlContextEntry.setKey(path + key);
            xmlContextEntry.setValue(ctx.get(key));
            entries.add(xmlContextEntry);
        }

        for (String child : ctx.getChildrenNames()) {
            toXmlContext(entries, path + child + "/", ctx.getChild(child));
        }
    }
}
