package org.openengsb.config.jbi.types;

public class ChoiceType extends AbstractType {
    private String values;

    public ChoiceType() {
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String[] getValues() {
        return values.split(",");
    }
}
