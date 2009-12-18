package org.openengsb.config.editor;

import org.apache.wicket.model.IModel;

public class BoolToStringModel implements IModel<Boolean> {
    private static final long serialVersionUID = 1L;
    private final IModel<String> model;

    public BoolToStringModel(IModel<String> model) {
        this.model = model;
    }

    @Override
    public Boolean getObject() {
        String v = model.getObject();
        return "1".equals(v) || "true".equals(v);
    }

    @Override
    public void setObject(Boolean object) {
        model.setObject(object.toString());
    }

    @Override
    public void detach() {
        // noop
    }
}