package org.openengsb.ui.web.tree;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class DropDownPanel extends Panel {

    private Log log = LogFactory.getLog(getClass());

    public DropDownPanel(String id, IModel<String> inputModel, IModel<List<String>> serviceList) {
        super(id);

        DropDownChoice<String> field = new DropDownChoice<String>("textfield", inputModel, serviceList);
        add(field);

        field.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                log.info("request caughed");

            }
        });
    }

   

}
