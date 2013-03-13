package org.openengsb.core.ekb.api;

import org.openengsb.core.api.Event;

public class CommitEvent extends Event {
    private EKBCommit commit;
    
    public CommitEvent() {
        
    }
    
    public CommitEvent(EKBCommit commit) {
        this.commit = commit;
    }
    
    public EKBCommit getCommit() {
        return commit;
    }
    
    public void setCommit(EKBCommit commit) {
        this.commit = commit;
    }
}
