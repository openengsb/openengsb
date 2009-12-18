package org.openengsb.config.editor.fields;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.openengsb.config.jbi.types.AbstractType;

public abstract class AbstractField extends Panel {
    private static final long serialVersionUID = 1L;
    private final AbstractType abstractType;
    private FormComponent<?> formComponent;

    public AbstractField(String id, AbstractType abstractType) {
        super(id);
        this.abstractType = abstractType;
    }

    public boolean isRequired() {
        return !abstractType.isOptional();
    }

    protected void setFormComponent(FormComponent<?> formComponent) {
        this.formComponent = formComponent;
    }

    public AbstractField setLabel(IModel<String> labelModel) {
        formComponent.setLabel(labelModel);
        return this;
    }
}
