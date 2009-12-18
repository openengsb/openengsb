package org.openengsb.config.editor.fields;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.openengsb.config.editor.validators.Validators;
import org.openengsb.config.jbi.types.AbstractType;

public class InputField extends AbstractField {
    private static final long serialVersionUID = 1L;

    public InputField(String id, IModel<String> model, AbstractType abstractType) {
        super(id, abstractType);
        TextField<String> tf = new TextField<String>("component", model);
        tf.setRequired(isRequired());
        tf.add(Validators.buildValidators(abstractType));
        add(tf);
        setFormComponent(tf);
    }
}
