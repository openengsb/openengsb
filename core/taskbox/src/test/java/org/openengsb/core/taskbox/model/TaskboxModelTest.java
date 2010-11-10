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

package org.openengsb.core.taskbox.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.taskbox.model.Ticket;
import org.openengsb.core.taskbox.model.TaskStepType;

public class TaskboxModelTest {
	
	private Ticket t;

    @Before
    public void init() throws Exception {
    	t = new Ticket("1");
    }

    @Test
    public void testCreateNewTicket() throws Exception {
        Ticket nt = new Ticket("nt1");
        if(nt == null)
        	fail();
        assertThat(nt.getId(), is("nt1"));
    }
    
    @Test
    public void testTicketId() throws Exception {
    	t.setId("2");
        assertThat(t.getId(), is("2"));
    }
    
    @Test
    public void testTicketType() throws Exception {
    	t.setType("TestTicketType");
        assertThat(t.getType(), is("TestTicketType"));
    }
    
    @Test
    public void testTicketHistoryNotes() throws Exception {
    	t = new Ticket("thn");
    	t.setType("type");
    	t.addNoteEntry("note1");
    	t.addNoteEntry("note2");
    	t.addNoteEntry("note3");
    	//Should be 5 HistoryEntrys and 3 NoteEntrys
    	int testPrimeProduct = t.getHistory().size()*t.getNotes().size();
    	assertThat(testPrimeProduct, is(15));
    }
    
    @Test
    public void testCompleteTicketInformationStep() throws Exception {
    	TaskStep ts = new CompleteTicketInformationStep("name", "desc");
    	assertThat(ts.getTaskStepTypeText(), is("CompleteTicketInformationStep"));
    }
    
    @Test
    public void testDeveloperTaskStep() throws Exception {
    	TaskStep ts = new DeveloperTaskStep("name", "desc");
    	assertThat(ts.getTaskStepType(), is(TaskStepType.DeveloperTaskStep));
    }
    
    @Test
    public void testInformationTaskStep() throws Exception {
    	TaskStep ts = new InformationTaskStep("name", "desc");
    	assertThat(ts.getTaskStepTypeText(), is("InformationTaskStep"));
    }
    
    @Test
    public void testReviewerTaskStep() throws Exception {
    	TaskStep ts = new ReviewerTaskStep("name", "desc");
    	System.out.println(ts.getTaskStepType());
    	assertThat(ts.getTaskStepTypeText(), is("ReviewerTaskStep"));
    }
    
    @Test
    public void testTicketCurrentTaskStep() throws Exception {
    	TaskStep test, ts = new DeveloperTaskStep("dev-name", "dev-desc");
    	t.setCurrentTaskStep(ts);
    	test = t.getCurrentTaskStep();
    	assertThat(ts.getName(), is(test.getName()));
    }
    
    @Test
    public void testTicketHistoryTaskSteps() throws Exception {
    	TaskStep ts = new ReviewerTaskStep("rev-name", "rev-desc");
    	t.setCurrentTaskStep(ts);
    	ts = new DeveloperTaskStep("dev-name", "dev-desc");
    	t.finishCurrentTaskStep(ts);
    	ts = new InformationTaskStep("inf-name", "inf-desc");
    	t.finishCurrentTaskStep(ts);
    	t.finishCurrentTaskStep();
    	assertThat(t.getHistoryTaskSteps().size(), is(3));
    }
    
    @Test
    public void testTicketFinishTaskSteps() throws Exception {
    	TaskStep ts = new ReviewerTaskStep("rev-name", "rev-desc");
    	t.setCurrentTaskStep(ts);
    	ts = t.finishCurrentTaskStep();
    	assertThat(ts.getDoneFlag(), is(true));
    }
}

