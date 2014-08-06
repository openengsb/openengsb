package org.openengsb.core.api.xlink.model;

import java.io.Serializable;
import java.util.List;

import org.openengsb.core.api.model.ModelDescription;

public class XLinkObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private Object modelObject;
    private ModelDescription modelDescription;
    private List<XLinkConnectorView> views;

    public XLinkObject() {

    }

    public XLinkObject(Object modelObject, ModelDescription modelDescription, List<XLinkConnectorView> views) {
        this.modelObject = modelObject;
        this.modelDescription = modelDescription;
        this.views = views;
    }

    public Object getModelObject() {
        return modelObject;
    }

    public void setModelObject(Object modelObject) {
        this.modelObject = modelObject;
    }

    public ModelDescription getModelDescription() {
        return modelDescription;
    }

    public void setModelDescription(ModelDescription modelDescription) {
        this.modelDescription = modelDescription;
    }

    public List<XLinkConnectorView> getViews() {
        return views;
    }

    public void setViews(List<XLinkConnectorView> views) {
        this.views = views;
    }
}
