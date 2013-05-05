package org.openengsb.core.ekb.common.models;

import java.util.List;

import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;

@Model
public class RecursiveModel {
    @OpenEngSBModelId
    private String id;
    private String value;

    private RecursiveModel child;

    private List<RecursiveModel> children;

    public RecursiveModel() {

    }

    public RecursiveModel(String id) {
        setId(id);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public RecursiveModel getChild() {
        return child;
    }

    public void setChild(RecursiveModel child) {
        this.child = child;
    }

    public List<RecursiveModel> getChildren() {
        return children;
    }

    public void setChildren(List<RecursiveModel> children) {
        this.children = children;
    }
}
