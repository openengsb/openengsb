package org.openengsb.ui.common.usermanagement;

import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.openengsb.core.api.PermissionProvider;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.ui.common.editor.BeanEditorPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PermissionEditorPanel extends Panel {

    private static final long serialVersionUID = 2009943701781924243L;

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionEditorPanel.class);

    private Panel editorPanel;

    private AjaxButton submitButton;

    private DropDownChoice<Class<?>> permissionTypeChoice;

    private Model<Class<?>> permissionTypeModel;

    private Map<String, String> values = Maps.newHashMap();

    private UserInput user;

    public PermissionEditorPanel(String id, UserInput user) {
        super(id);
        this.user = user;
        init();
    }

    private void init() {

        final WebMarkupContainer container = new WebMarkupContainer("container");
        add(container);
        container.setOutputMarkupId(true);

        Form<?> form = new Form<Object>("form");
        container.add(form);

        permissionTypeModel = new Model<Class<?>>();
        LoadableDetachableModel<List<Class<?>>> permissionTypeListModel =
            new LoadableDetachableModel<List<Class<?>>>() {
                private static final long serialVersionUID = -3960517945144173704L;

                @Override
                protected List<Class<?>> load() {
                    List<PermissionProvider> providers =
                        OpenEngSBCoreServices.getServiceUtilsService().listServices(PermissionProvider.class);
                    List<Class<?>> result = Lists.newArrayList();
                    for (PermissionProvider p : providers) {
                        result.addAll(p.getSupportedPermissionClasses());
                    }
                    return result;
                }
            };

        submitButton = new AjaxButton("submitButton") {
            private static final long serialVersionUID = 6787520770396648012L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> arg1) {
                Class<?> permissionClass = permissionTypeModel.getObject();
                user.getNewPermissions().add(new PermissionInput(permissionClass, values));
                editorPanel.replaceWith(new EmptyPanel("permissionEditor"));
                submitButton.setVisible(false);
                target.addComponent(container);
                LOGGER.info("got values {}", values.toString());
            }
        };
        form.add(submitButton);
        submitButton.setVisible(false);

        permissionTypeChoice =
            new DropDownChoice<Class<?>>("permissionTypeSelect", permissionTypeModel, permissionTypeListModel);
        permissionTypeChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = 5195539410268926662L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                LOGGER.info("selected + " + permissionTypeModel.getObject());
                BeanEditorPanel beanEditorPanel =
                    new BeanEditorPanel("permissionEditor", permissionTypeModel.getObject(), values);
                editorPanel.replaceWith(beanEditorPanel);
                editorPanel = beanEditorPanel;
                submitButton.setVisible(true);
                target.addComponent(container);
            }
        });

        form.add(permissionTypeChoice);

        editorPanel = new EmptyPanel("permissionEditor");

        form.add(editorPanel);

    }
}