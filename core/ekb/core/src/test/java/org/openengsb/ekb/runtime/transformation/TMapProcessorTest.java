/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ekb.runtime.transformation;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Assert;
import org.junit.Test;
import org.openengsb.ekb.resources.OpenEngSBMessage;

public class TMapProcessorTest {

    @Test
    public void testTMapProcessorListOfURL() throws Exception {
        ArrayList<URL> tmapList = new ArrayList<URL>();
        URL tmap = TMapProcessor.class.getResource("tmap1.xml");
        tmapList.add(tmap);
        new TMapProcessor(tmapList);
    }

    @Test
    public void testTransform() throws Exception {
        ArrayList<URL> tmapList = new ArrayList<URL>();
        URL tmap = TMapProcessor.class.getResource("tmap1.xml");
        tmapList.add(tmap);
        TMapProcessor proc = new TMapProcessor(tmapList);

        Document inputDocument = loadMessage("messageIn.xml");
        OpenEngSBMessage inputMessage = new OpenEngSBMessage(inputDocument);
        OpenEngSBMessage outputMessage = proc.transform(inputMessage);

        Document expectedDocument = loadMessage("messageOut.xml");
        Document realDocument = outputMessage.toXML();

        DetailedDiff myDiff = new DetailedDiff(new Diff(expectedDocument.asXML(), realDocument.asXML()));
        Assert.assertTrue(myDiff.toString(), myDiff.similar());
    }

    private Document loadMessage(String messageFileName) throws Exception {
        InputStream in = TMapProcessor.class.getResource(messageFileName).openStream();
        SAXReader reader = new SAXReader();
        return reader.read(in);
    }

}
