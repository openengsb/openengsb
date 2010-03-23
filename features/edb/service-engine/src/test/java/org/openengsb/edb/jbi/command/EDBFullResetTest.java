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
package org.openengsb.edb.jbi.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.common.util.MessageUtil.NormalizedMessageImpl;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.core.api.impl.DefaultEDBHandler;
import org.openengsb.edb.jbi.endpoints.commands.EDBFullReset;

public class EDBFullResetTest {

    private EDBHandler handler = mock(DefaultEDBHandler.class);
    private EDBFullReset resetCommand = new EDBFullReset(handler, LogFactory.getLog(EDBFullResetTest.class));
    private NormalizedMessage msg;

    private static final String MESSAGE_TYPE_FULLRESET = "acmResetFullRequestMessage";
    private static Document fullResetMessage;

    @Before
    public void setUp() throws Exception {
        makeParameters();
        msg = new NormalizedMessageImpl();
        msg.setContent(new StringSource(fullResetMessage.asXML()));
    }

    @Test
    public void testExecuteCallsHandler() throws Exception {
        resetCommand.execute(msg);
        verify(handler).removeRepository();
    }

    private static void makeParameters() {
        /* valid full reset */
        Element root = DocumentHelper.createElement(MESSAGE_TYPE_FULLRESET);
        final Element resetBody = root.addElement("body");
        resetBody.addElement("repoId").setText("_default_");
        EDBFullResetTest.fullResetMessage = DocumentHelper.createDocument(root);

    }

}
