
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:element xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLContext">
 *   &lt;xs:complexType>
 *     &lt;xs:sequence>
 *       &lt;xs:element type="XMLContextEntry" name="entries" maxOccurs="unbounded"/>
 *     &lt;/xs:sequence>
 *   &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 */
public class XMLContext
{
    private List<XMLContextEntry> entryList = new ArrayList<XMLContextEntry>();

    /** 
     * Get the list of 'entries' element items.
     * 
     * @return list
     */
    public List<XMLContextEntry> getEntries() {
        return entryList;
    }

    /** 
     * Set the list of 'entries' element items.
     * 
     * @param list
     */
    public void setEntries(List<XMLContextEntry> list) {
        entryList = list;
    }
}
