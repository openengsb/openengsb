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
package org.openengsb.connector.svn.test;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.connector.svn.RepositoryPoller;
import org.openengsb.connector.svn.SvnConnector;
import org.openengsb.connector.svn.UpdateResult;
import org.openengsb.core.EventHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;
import org.openengsb.core.model.Event;
import org.openengsb.drools.events.ScmBranchCreatedEvent;
import org.openengsb.drools.events.ScmBranchDeletedEvent;
import org.openengsb.drools.events.ScmCheckInEvent;
import org.openengsb.drools.events.ScmDirectoryEvent;
import org.openengsb.drools.events.ScmTagCreatedEvent;
import org.openengsb.drools.events.ScmTagDeletedEvent;

public class RepositoryPollerTest {
    private RepositoryPoller poller;
    private SvnConnector connector;
    private EventHelper eventHelper;

    @Before
    public void setUp() throws IOException {
        connector = Mockito.mock(SvnConnector.class);
        eventHelper = Mockito.mock(EventHelper.class);
        
        OpenEngSBEndpoint endpoint = Mockito.mock(OpenEngSBEndpoint.class);
        Mockito.when(endpoint.createEventHelper(Mockito.any(MessageProperties.class))).thenReturn(eventHelper);
        
        poller = new RepositoryPoller();
        poller.setConnector(connector);
        poller.setEndpoint(endpoint);
    }
    
    @Test
    public void testPollRepositoryEmpty() throws IOException {
        Mockito.when(connector.update()).thenReturn(new UpdateResult());
        poller.poll();
        
        Mockito.verify(eventHelper, Mockito.never()).sendEvent(Mockito.any(Event.class));
    }

    @Test
    public void testPollRepository() throws IOException {
        Mockito.when(connector.update()).thenReturn(prepareUpdateResult());
        poller.poll();
        
        ScmDirectoryEvent e = new ScmBranchCreatedEvent();
        e.setDirectory("branch1");
        Mockito.verify(eventHelper, Mockito.times(1)).sendEvent(Mockito.eq(e));
        e.setDirectory("branch2");
        Mockito.verify(eventHelper, Mockito.times(1)).sendEvent(Mockito.eq(e));
        
        e = new ScmTagCreatedEvent();
        e.setDirectory("tag1");
        Mockito.verify(eventHelper, Mockito.times(1)).sendEvent(Mockito.eq(e));
        e.setDirectory("tag2");
        Mockito.verify(eventHelper, Mockito.times(1)).sendEvent(Mockito.eq(e));
        
        e = new ScmCheckInEvent();
        e.setDirectory("commit1");
        Mockito.verify(eventHelper, Mockito.times(1)).sendEvent(Mockito.eq(e));
        e.setDirectory("commit2");
        Mockito.verify(eventHelper, Mockito.times(1)).sendEvent(Mockito.eq(e));
        
        e = new ScmBranchDeletedEvent();
        e.setDirectory("branch3");
        Mockito.verify(eventHelper, Mockito.times(1)).sendEvent(Mockito.eq(e));
        e.setDirectory("branch4");
        Mockito.verify(eventHelper, Mockito.times(1)).sendEvent(Mockito.eq(e));
        
        e = new ScmTagDeletedEvent();
        e.setDirectory("tag3");
        Mockito.verify(eventHelper, Mockito.times(1)).sendEvent(Mockito.eq(e));
        e.setDirectory("tag4");
        Mockito.verify(eventHelper, Mockito.times(1)).sendEvent(Mockito.eq(e));
    }
    
    private UpdateResult prepareUpdateResult() {
        UpdateResult result = new UpdateResult();
        result.getAddedBranches().add("branch1");
        result.getAddedBranches().add("branch2");
        
        result.getAddedTags().add("tag1");
        result.getAddedTags().add("tag2");
        
        result.getCommitted().add("commit1");
        result.getCommitted().add("commit2");
        
        result.getDeletedBranches().add("branch3");
        result.getDeletedBranches().add("branch4");
        
        result.getDeletedTags().add("tag3");
        result.getDeletedTags().add("tag4");
        
        return result;
    }

}
