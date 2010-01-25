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
package org.openengsb.connector.svn;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.EventHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.SimpleEventEndpoint;
import org.openengsb.core.model.Event;
import org.openengsb.drools.events.ScmCheckInEvent;
import org.openengsb.drools.model.MergeResult;

/**
 * @org.apache.xbean.XBean element="eventEndpoint"
 *                         description="SVN event endpoint"
 */
public class SvnEventEndpoint extends SimpleEventEndpoint {

    private SvnScmImplementation svn;

    @Override
    protected void handleEvent(Event e, ContextHelper contextHelper, MessageProperties msgProperties) {
        if (!(e instanceof ScmCheckInEvent)) {
            return;
        }
        ScmCheckInEvent event = (ScmCheckInEvent) e;
        MergeResult checkoutResult = svn.checkout("openengsb");
        event.setRevision(checkoutResult.getRevision());
        EventHelper eventHelper = createEventHelper(msgProperties);
        eventHelper.sendEvent(event);
    }

    public void setConfiguration(SvnConfiguration configuration) {
        svn = new SvnScmImplementation(configuration);
    }

}
