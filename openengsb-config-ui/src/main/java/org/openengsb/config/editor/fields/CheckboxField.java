package org.openengsb.config.editor.fields;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.openengsb.config.editor.BoolToStringModel;
import org.openengsb.config.jbi.types.AbstractType;

public class CheckboxField extends AbstractField {
    private static final long serialVersionUID = 1L;
    private final CheckBox cb;

    public CheckboxField(String id, IModel<String> model, AbstractType abstractType) {
        super(id, abstractType);
        cb = new CheckBox("component", new BoolToStringModel(model));
        add(cb);
        setFormComponent(cb);
    }
}
