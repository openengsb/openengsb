
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLBean">
 *   &lt;xs:sequence>
 *     &lt;xs:element type="xs:string" name="className"/>
 *     &lt;xs:element type="XMLField" name="fields" maxOccurs="unbounded"/>
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class XMLBean
{
    private String className;
    private List<XMLField> fieldList = new ArrayList<XMLField>();

    /** 
     * Get the 'className' element value.
     * 
     * @return value
     */
    public String getClassName() {
        return className;
    }

    /** 
     * Set the 'className' element value.
     * 
     * @param className
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /** 
     * Get the list of 'fields' element items.
     * 
     * @return list
     */
    public List<XMLField> getFields() {
        return fieldList;
    }

    /** 
     * Set the list of 'fields' element items.
     * 
     * @param list
     */
    public void setFields(List<XMLField> list) {
        fieldList = list;
    }
}
