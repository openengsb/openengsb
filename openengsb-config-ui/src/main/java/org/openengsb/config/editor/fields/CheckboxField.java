package org.openengsb.config.editor.fields;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.Model;
import org.openengsb.config.jbi.types.AbstractType;

public class CheckboxField extends AbstractField {
    private static final long serialVersionUID = 1L;

    public CheckboxField(String id, AbstractType abstractType) {
        super(id, abstractType);
        CheckBox cb = new CheckBox("component");
        cb.setLabel(new Model<String>(abstractType.getName()));
        add(cb);
    }
}
