package org.openengsb.config.editor;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.openengsb.config.editor.fields.CheckboxField;
import org.openengsb.config.editor.fields.DropdownChoiceField;
import org.openengsb.config.editor.fields.InputField;
import org.openengsb.config.jbi.types.AbstractType;
import org.openengsb.config.jbi.types.BoolType;
import org.openengsb.config.jbi.types.ChoiceType;
import org.openengsb.config.jbi.types.EndpointType;

public abstract class EditorPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private final EndpointType endpointType;
    private final String componentId;

    public EditorPanel(String id, String componentId, EndpointType endpointType) {
        this(id, componentId, endpointType, null);
    }

    public EditorPanel(String id, String componentId, EndpointType endpointType, IModel<?> model) {
        super(id, model);
        this.componentId = componentId;
        this.endpointType = endpointType;
        createForm();
    }

    public abstract void onSubmit();

    private void createForm() {
        Form<?> form = new Form("form") {
            @Override
            protected void onSubmit() {
                EditorPanel.this.onSubmit();
            }
        };
        add(form);

        RepeatingView fields = new RepeatingView("fields");
        form.add(fields);
        form.add(new FeedbackPanel("feedback"));

        for (AbstractType f : endpointType.getAttributes()) {
            WebMarkupContainer row = new WebMarkupContainer(fields.newChildId());
            fields.add(row);
            row.add(new Label("name", new ResourceModel(componentId + '.' + endpointType.getName() + '.' + f.getName())));
            row.add(getEditor(f));
        }
    }

    private Component getEditor(AbstractType type) {
        if (type.getClass().equals(BoolType.class))
            return new CheckboxField("editor", type);
        else if (type.getClass().equals(ChoiceType.class))
            return new DropdownChoiceField("editor", (ChoiceType)type);
        else
            return new InputField("editor", type);
    }
}
