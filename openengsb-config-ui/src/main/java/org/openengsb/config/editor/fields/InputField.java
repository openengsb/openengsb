package org.openengsb.config.editor.fields;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.openengsb.config.jbi.types.AbstractType;

public class InputField extends AbstractField {
    private static final long serialVersionUID = 1L;

    public InputField(String id, AbstractType abstractType) {
        super(id, abstractType);
        TextField<String> tf = new TextField<String>("component");
        tf.setLabel(new Model<String>(abstractType.getName()));
        tf.setRequired(isRequired());
        add(tf);
    }
}
