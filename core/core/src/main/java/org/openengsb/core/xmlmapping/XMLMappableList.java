
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class XMLMappableList
{
    private List<XMLMappable> mappableList = new ArrayList<XMLMappable>();

    @XmlElement(required = true)
    public List<XMLMappable> getMappables() {
        return mappableList;
    }

    public void setMappables(List<XMLMappable> list) {
        mappableList = list;
    }
}
