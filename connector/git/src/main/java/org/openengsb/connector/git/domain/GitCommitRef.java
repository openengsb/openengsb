package org.openengsb.connector.git.domain;

import org.eclipse.jgit.lib.AnyObjectId;
import org.openengsb.domain.scm.CommitRef;

public class GitCommitRef implements CommitRef {
    private AnyObjectId commitRef;
    
    public GitCommitRef(AnyObjectId commitRef) {
        this.commitRef = commitRef;
    }

    @Override
    public String getStringRepresentation() {
        return this.commitRef.name();
    }
}
