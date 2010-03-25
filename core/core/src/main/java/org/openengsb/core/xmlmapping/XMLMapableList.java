
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLMapableList">
 *   &lt;xs:sequence>
 *     &lt;xs:element type="XMLMapable" name="mapable" maxOccurs="unbounded"/>
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class XMLMapableList
{
    private List<XMLMapable> mapableList = new ArrayList<XMLMapable>();

    /** 
     * Get the list of 'mapable' element items.
     * 
     * @return list
     */
    public List<XMLMapable> getMapables() {
        return mapableList;
    }

    /** 
     * Set the list of 'mapable' element items.
     * 
     * @param list
     */
    public void setMapables(List<XMLMapable> list) {
        mapableList = list;
    }
}
