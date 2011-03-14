package org.openengsb.core.workflow.editor.converter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class ActionNode {

    @XmlAttribute
    private int id;
    @XmlAttribute
    private String name;

    private Action action;

    public ActionNode() {
    }

    public ActionNode(int id, String name, String location, String methodName) {
        super();
        this.id = id;
        this.name = name;
        this.action = new Action();
        action.setValue(location + "." + methodName + "()");
    }

    public static class Action {
        @XmlAttribute
        private String type = "expression";
        @XmlAttribute
        private String dialect = "java";

        @XmlValue
        private String value;

        public void setValue(String value) {
            this.value = value;
        }
    }
}
