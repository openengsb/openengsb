package org.openengsb.core.api.workflow.model;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Test;

public class ActionRepresentationTest {
    @Test
    public void hasNoActionsOrEvents_ShouldBeLeaf() {
        ActionRepresentation action = new ActionRepresentation();
        assertThat(action.isLeaf(), equalTo(true));
        EndRepresentation end = new EndRepresentation();
        action.setEnd(end);
        assertThat(action.getEnd(), sameInstance(end));
    }

    @Test
    public void hasActions_ShouldNotBeLeaf() {
        ActionRepresentation action = new ActionRepresentation();
        action.addAction(new ActionRepresentation());
        assertThat(action.isLeaf(), equalTo(false));
        assertThat(action.getEnd(), equalTo(null));
    }

    @Test
    public void hasEvent_ShouldNotBeLeaf() {
        ActionRepresentation action = new ActionRepresentation();
        action.addEvent(new EventRepresentation());
        assertThat(action.isLeaf(), equalTo(false));
        assertThat(action.getEnd(), equalTo(null));
    }

    @Test
    public void hasNoEnd_ShoulReturnFalseForHasSharedEnd() {
        assertFalse(new ActionRepresentation().hasSharedEnd());
    }

    @Test
    public void hasSharedEnd_ShoulReturnTrueForHasSharedEnd() {
        ActionRepresentation action = new ActionRepresentation();
        action.setEnd(new EndRepresentation());
        assertTrue(action.hasSharedEnd());
    }
}
