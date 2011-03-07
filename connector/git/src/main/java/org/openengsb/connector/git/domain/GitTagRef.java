package org.openengsb.connector.git.domain;

import org.eclipse.jgit.revwalk.RevTag;
import org.openengsb.domain.scm.TagRef;

public class GitTagRef implements TagRef {
    private RevTag tagRef;

    public GitTagRef(RevTag tagRef) {
        this.tagRef = tagRef;
    }

    @Override
    public String getTagName() {
        if (tagRef == null) {
            return null;
        }
        return tagRef.getTagName();
    }

    @Override
    public String getStringRepresentation() {
        if (tagRef == null) {
            return null;
        }
        return tagRef.name();
    }

}
