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

package org.openengsb.context;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;
import org.openengsb.util.serialization.JibxXmlSerializer;
import org.openengsb.util.serialization.SerializationException;

public class ContextToSegmentTransformer {

    public static Segment transform(Context ctx) {
        return transform("/", ctx);
    }

    public static Segment transform(String current, Context ctx) {

        List<Segment> list = new ArrayList<Segment>();

        for (String key : ctx.getKeys()) {
            Segment text = new TextSegment.Builder().name(key).text(ctx.get(key)).build();
            list.add(text);
        }

        for (String child : ctx.getChildrenNames()) {
            list.add(transform(child, ctx.getChild(child)));
        }

        Segment root = new ListSegment.Builder().name(current).list(list).build();

        return root;
    }

    public static String asString(Segment segment) throws SerializationException {
        StringWriter writer = new StringWriter();
        new JibxXmlSerializer().serialize(segment, writer);
        return writer.toString();
    }
}
