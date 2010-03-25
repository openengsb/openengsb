
package org.openengsb.core.xmlmapping;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLField">
 *   &lt;xs:sequence>
 *     &lt;xs:element type="xs:string" name="className"/>
 *     &lt;xs:element type="XMLMapable" name="value"/>
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class XMLField
{
    private String className;
    private XMLMapable value;

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
     * Get the 'value' element value.
     * 
     * @return value
     */
    public XMLMapable getValue() {
        return value;
    }

    /** 
     * Set the 'value' element value.
     * 
     * @param value
     */
    public void setValue(XMLMapable value) {
        this.value = value;
    }
}
