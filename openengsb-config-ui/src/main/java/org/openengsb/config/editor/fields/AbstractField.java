package org.openengsb.config.editor.fields;

import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.config.jbi.types.AbstractType;

public class AbstractField extends Panel {
    private static final long serialVersionUID = 1L;
    private final AbstractType abstractType;

    public AbstractField(String id, AbstractType abstractType) {
        super(id);
        this.abstractType = abstractType;
    }

    public boolean isRequired() {
        return !abstractType.isOptional();
    }
}
