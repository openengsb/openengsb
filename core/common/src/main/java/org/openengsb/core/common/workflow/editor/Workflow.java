package org.openengsb.core.common.workflow.editor;

public class Workflow {

    private String name;
    private Action root = new Action();

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final Action getRoot() {
        return root;
    }

    public final void setRoot(Action root) {
        this.root = root;
    }
}
