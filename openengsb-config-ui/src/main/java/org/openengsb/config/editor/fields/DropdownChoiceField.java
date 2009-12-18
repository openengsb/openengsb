package org.openengsb.config.editor.fields;

import java.util.Arrays;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.openengsb.config.jbi.types.ChoiceType;

public class DropdownChoiceField extends AbstractField {
    private static final long serialVersionUID = 1L;

    public DropdownChoiceField(String id, IModel<String> model, ChoiceType choiceType) {
        super(id, choiceType);
        DropDownChoice<String> ch = new DropDownChoice<String>("component", model, Arrays.asList(choiceType.getValues()));
        ch.setRequired(isRequired());
        add(ch);
        setFormComponent(ch);
    }
}
