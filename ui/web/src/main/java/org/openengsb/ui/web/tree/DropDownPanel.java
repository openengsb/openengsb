package org.openengsb.ui.web.tree;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class DropDownPanel extends Panel {

    

    public DropDownPanel(String id, IModel<String> inputModel, IModel<List<String>> serviceList) {
        super(id);

        DropDownChoice<String> field = new DropDownChoice<String>("textfield", inputModel, serviceList);
        add(field);

        field.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
    }

   

}
