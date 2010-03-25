
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLEvent">
 *   &lt;xs:sequence>
 *     &lt;xs:element type="xs:string" name="className"/>
 *     &lt;xs:element type="xs:string" name="domain"/>
 *     &lt;xs:element type="xs:string" name="name"/>
 *     &lt;xs:element type="xs:string" nillable="true" name="toolConnector"/>
 *     &lt;xs:element type="XMLMapEntry" name="elements" maxOccurs="unbounded"/>
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class XMLEvent
{
    private String className;
    private String domain;
    private String name;
    private String toolConnector;
    private List<XMLMapEntry> elementList = new ArrayList<XMLMapEntry>();

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
     * Get the 'domain' element value.
     * 
     * @return value
     */
    public String getDomain() {
        return domain;
    }

    /** 
     * Set the 'domain' element value.
     * 
     * @param domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /** 
     * Get the 'name' element value.
     * 
     * @return value
     */
    public String getName() {
        return name;
    }

    /** 
     * Set the 'name' element value.
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /** 
     * Get the 'toolConnector' element value.
     * 
     * @return value
     */
    public String getToolConnector() {
        return toolConnector;
    }

    /** 
     * Set the 'toolConnector' element value.
     * 
     * @param toolConnector
     */
    public void setToolConnector(String toolConnector) {
        this.toolConnector = toolConnector;
    }

    /** 
     * Get the list of 'elements' element items.
     * 
     * @return list
     */
    public List<XMLMapEntry> getElements() {
        return elementList;
    }

    /** 
     * Set the list of 'elements' element items.
     * 
     * @param list
     */
    public void setElements(List<XMLMapEntry> list) {
        elementList = list;
    }
}
