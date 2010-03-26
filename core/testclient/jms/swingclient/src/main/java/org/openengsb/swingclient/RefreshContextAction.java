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
import java.lang.reflect.Method;

import javax.jms.JMSException;

import org.openengsb.contextcommon.Context;
import org.openengsb.contextcommon.ContextHelperExtended;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public class RefreshContextAction implements ActionListener {

    private ContextPanel panel;

    public RefreshContextAction(ContextPanel panel) {
        this.panel = panel;

    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        try {
            String result = OpenEngSBClient.contextCall(getMessage());
            ReturnValue returnValue = Transformer.toReturnValue(result);
            Context context = (Context) returnValue.getValue();
            panel.tree.updateTree(context);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    private String getMessage() {
        try {
            Method method = ContextHelperExtended.class.getMethod("getContext", String.class);
            MethodCall call = new MethodCall(method, new Object[] { "/" });
            String xml = Transformer.toXml(call);
            return xml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
