package org.openengsb.connector.git.domain;

import org.eclipse.jgit.lib.AnyObjectId;
import org.openengsb.domain.scm.TagRef;

public class GitTagRef implements TagRef {
    private AnyObjectId tagRef;

    public GitTagRef(AnyObjectId tagRef) {
        this.tagRef = tagRef;
    }

    @Override
    public String getStringRepresentation() {
        if (tagRef == null) {
            return null;
        }
        return tagRef.name();
    }

}
