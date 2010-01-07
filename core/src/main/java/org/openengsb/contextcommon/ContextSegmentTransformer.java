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

import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;

public class ContextSegmentTransformer {

    public static Segment toSegment(Context ctx) {
        return toSegment("/", ctx);
    }

    public static Context toContext(Segment segment) {
        ContextStore store = new ContextStore();
        toContext("", store, segment);
        return store.getContext("/");
    }

    private static void toContext(String prefix, ContextStore store, Segment segment) {
        if (segment instanceof TextSegment) {
            TextSegment ts = (TextSegment) segment;
            String value = ts.getText();
            String key = ts.getName();

            store.setValue(prefix + key, value);
        } else if (segment instanceof ListSegment) {
            ListSegment ls = (ListSegment) segment;

            for (Segment s : ls.getList()) {
                toContext(prefix + ls.getName() + "/", store, s);
            }
        }
    }

    private static Segment toSegment(String current, Context ctx) {
        List<Segment> list = new ArrayList<Segment>();

        for (String key : ctx.getKeys()) {
            Segment text = new TextSegment.Builder().name(key).text(ctx.get(key)).build();
            list.add(text);
        }

        for (String child : ctx.getChildrenNames()) {
            list.add(toSegment(child, ctx.getChild(child)));
        }

        Segment root = new ListSegment.Builder().name(current).list(list).build();

        return root;
    }
}
