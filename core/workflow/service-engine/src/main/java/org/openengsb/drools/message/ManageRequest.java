package org.openengsb.drools.message;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.openengsb.drools.source.RuleBaseElement;

@XmlRootElement
@XmlType(propOrder = { "elementType", "name", "code" })
public class ManageRequest {
    private RuleBaseElement elementType;
    private String name;
    private String code;

    public RuleBaseElement getElementType() {
        return this.elementType;
    }

    public void setElementType(RuleBaseElement elementType) {
        this.elementType = elementType;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
