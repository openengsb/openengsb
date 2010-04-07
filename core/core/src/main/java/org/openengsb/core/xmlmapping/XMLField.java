
package org.openengsb.core.xmlmapping;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLField">
 *   &lt;xs:sequence>
 *     &lt;xs:element type="xs:string" name="fieldName"/>
 *     &lt;xs:element type="XMLMappable" name="value"/>
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class XMLField
{
    private String fieldName;
    private XMLMappable value;

    /** 
     * Get the 'fieldName' element value.
     * 
     * @return value
     */
    public String getFieldName() {
        return fieldName;
    }

    /** 
     * Set the 'fieldName' element value.
     * 
     * @param fieldName
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /** 
     * Get the 'value' element value.
     * 
     * @return value
     */
    public XMLMappable getValue() {
        return value;
    }

    /** 
     * Set the 'value' element value.
     * 
     * @param value
     */
    public void setValue(XMLMappable value) {
        this.value = value;
    }
}
