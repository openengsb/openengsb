
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:element xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLMethodCall">
 *   &lt;xs:complexType>
 *     &lt;xs:sequence>
 *       &lt;xs:element type="xs:string" name="methodName"/>
 *       &lt;xs:element type="XMLTypedValue" name="args" maxOccurs="unbounded"/>
 *     &lt;/xs:sequence>
 *     &lt;xs:attribute type="xs:string" name="domainConcept"/>
 *   &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 */
public class XMLMethodCall
{
    private String methodName;
    private List<XMLTypedValue> argList = new ArrayList<XMLTypedValue>();
    private String domainConcept;

    /** 
     * Get the 'methodName' element value.
     * 
     * @return value
     */
    public String getMethodName() {
        return methodName;
    }

    /** 
     * Set the 'methodName' element value.
     * 
     * @param methodName
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /** 
     * Get the list of 'args' element items.
     * 
     * @return list
     */
    public List<XMLTypedValue> getArgs() {
        return argList;
    }

    /** 
     * Set the list of 'args' element items.
     * 
     * @param list
     */
    public void setArgs(List<XMLTypedValue> list) {
        argList = list;
    }

    /** 
     * Get the 'domainConcept' attribute value.
     * 
     * @return value
     */
    public String getDomainConcept() {
        return domainConcept;
    }

    /** 
     * Set the 'domainConcept' attribute value.
     * 
     * @param domainConcept
     */
    public void setDomainConcept(String domainConcept) {
        this.domainConcept = domainConcept;
    }
}
