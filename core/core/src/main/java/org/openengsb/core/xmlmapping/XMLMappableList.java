
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLMappableList">
 *   &lt;xs:sequence>
 *     &lt;xs:element type="XMLMappable" name="mappable" maxOccurs="unbounded"/>
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class XMLMappableList
{
    private List<XMLMappable> mappableList = new ArrayList<XMLMappable>();

    /** 
     * Get the list of 'mappable' element items.
     * 
     * @return list
     */
    public List<XMLMappable> getMappables() {
        return mappableList;
    }

    /** 
     * Set the list of 'mappable' element items.
     * 
     * @param list
     */
    public void setMappables(List<XMLMappable> list) {
        mappableList = list;
    }
}
