package org.openengsb.config.editor;

import java.util.List;

import org.openengsb.config.jbi.types.AbstractType;

public class FieldInfos {
    private final String name;
    private final List<AbstractType> fieldTypes;

    public FieldInfos(String name, List<AbstractType> fieldTypes) {
        super();
        this.name = name;
        this.fieldTypes = fieldTypes;
    }

    public String getName() {
        return name;
    }

    public List<AbstractType> getFieldTypes() {
        return fieldTypes;
    }
}
