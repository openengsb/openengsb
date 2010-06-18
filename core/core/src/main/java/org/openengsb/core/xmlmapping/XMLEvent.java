
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = { "className", "domain", "name", "toolConnector", "elements" })
public class XMLEvent
{
    private String className;
    private String domain;
    private String name;
    private String toolConnector;
    private List<XMLMapEntry> elementList = new ArrayList<XMLMapEntry>();

    @XmlElement(required = true)
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @XmlElement(required = true)
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @XmlElement(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(required = true, nillable = true)
    public String getToolConnector() {
        return toolConnector;
    }

    public void setToolConnector(String toolConnector) {
        this.toolConnector = toolConnector;
    }

    @XmlElement(required = true)
    public List<XMLMapEntry> getElements() {
        return elementList;
    }

    public void setElements(List<XMLMapEntry> list) {
        elementList = list;
    }
}
