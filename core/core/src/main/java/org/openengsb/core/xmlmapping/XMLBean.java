
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "className", "fields" })
public class XMLBean
{
    private String className;
    private List<XMLField> fieldList = new ArrayList<XMLField>();

    @XmlElement(required = true)
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @XmlElement(required = true)
    public List<XMLField> getFields() {
        return fieldList;
    }

    public void setFields(List<XMLField> list) {
        fieldList = list;
    }
}
