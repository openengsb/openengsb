package org.openengsb.config.editor;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.openengsb.config.editor.fields.CheckboxField;
import org.openengsb.config.jbi.types.AbstractType;
import org.openengsb.config.jbi.types.EndpointType;

public abstract class EditorPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private final EndpointType endpointType;

    public EditorPanel(String id, EndpointType endpointType) {
        this(id, endpointType, null);
    }

    public EditorPanel(String id, EndpointType endpointType, IModel<?> model) {
        super(id, model);
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
            row.add(new Label("name", f.getName()));
            row.add(getEditor(f));
        }
    }

    private Component getEditor(AbstractType type) {
       return new CheckboxField("editor", type);
    }
}
