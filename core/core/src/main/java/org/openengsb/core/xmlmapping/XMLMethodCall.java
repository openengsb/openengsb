
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = { "methodName", "args" })
public class XMLMethodCall
{
    private String methodName;
    private List<XMLTypedValue> argList = new ArrayList<XMLTypedValue>();
    private String domainConcept;

    @XmlElement(required = true)
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @XmlElement(required = true)
    public List<XMLTypedValue> getArgs() {
        return argList;
    }

    public void setArgs(List<XMLTypedValue> list) {
        argList = list;
    }

    @XmlAttribute(required = true)
    public String getDomainConcept() {
        return domainConcept;
    }

    public void setDomainConcept(String domainConcept) {
        this.domainConcept = domainConcept;
    }
}
