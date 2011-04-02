package org.openengsb.core.workflow.editor;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Test;

public class ActionTest {
    @Test
    public void hasNoActionsOrEvents_ShouldBeLeaf() {
        Action action = new Action();
        assertThat(action.isLeaf(), equalTo(true));
        End end = new End();
        action.setEnd(end);
        assertThat(action.getEnd(), sameInstance(end));
    }

    @Test
    public void hasActions_ShouldNotBeLeaf() {
        Action action = new Action();
        action.addAction(new Action());
        assertThat(action.isLeaf(), equalTo(false));
        assertThat(action.getEnd(), equalTo(null));
    }

    @Test
    public void hasEvent_ShouldNotBeLeaf() {
        Action action = new Action();
        action.addEvent(new Event());
        assertThat(action.isLeaf(), equalTo(false));
        assertThat(action.getEnd(), equalTo(null));
    }

    @Test
    public void hasNoEnd_ShoulReturnFalseForHasSharedEnd() {
        assertFalse(new Action().hasSharedEnd());
    }

    @Test
    public void hasSharedEnd_ShoulReturnTrueForHasSharedEnd() {
        Action action = new Action();
        action.setEnd(new End());
        assertTrue(action.hasSharedEnd());
    }
}
