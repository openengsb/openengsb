
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
 *       &lt;xs:element type="XMLMapable" name="args" maxOccurs="unbounded"/>
 *     &lt;/xs:sequence>
 *   &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 */
public class XMLMethodCall
{
    private String methodName;
    private List<XMLMapable> argList = new ArrayList<XMLMapable>();

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
    public List<XMLMapable> getArgs() {
        return argList;
    }

    /** 
     * Set the list of 'args' element items.
     * 
     * @param list
     */
    public void setArgs(List<XMLMapable> list) {
        argList = list;
    }
}
