package org.openengsb.drools.message;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class RuleBaseElementId {

    public static final String DEFAULT_RULE_PACKAGE = "org.openengsb";

    private RuleBaseElementType type;
    private String packageName;
    private String name;

    public RuleBaseElementId(RuleBaseElementType type, String name) {
        this.type = type;
        this.name = name;
        this.packageName = DEFAULT_RULE_PACKAGE;
    }

    public RuleBaseElementId(RuleBaseElementType type, String packageName, String name) {
        this.type = type;
        this.packageName = packageName;
        this.name = name;
    }

    public RuleBaseElementId() {
        this.packageName = DEFAULT_RULE_PACKAGE;
    }

    public RuleBaseElementType getType() {
        return this.type;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getName() {
        return this.name;
    }

    public void setType(RuleBaseElementType type) {
        this.type = type;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setName(String name) {
        this.name = name;
    }

}
