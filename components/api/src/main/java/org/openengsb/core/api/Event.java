/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.api;

import javax.xml.bind.annotation.XmlRootElement;

import org.openengsb.core.api.model.OpenEngSBModel;

@XmlRootElement
public interface Event extends OpenEngSBModel {
    
    String getName();
    
    void setName(String name);
    
    Long getProcessId();
    
    void setProcessId(Long processId);
    
    String getOrigin();
    
    void setOrigin(String origin);
    
    String getType();
    
    void setType(String type);
    

    // private String name;
    // private Long processId;
    // private String origin;
    //
    // public Event() {
    // }
    //
    // public Event(String name) {
    // this.name = name;
    // }
    //
    // public Event(Long processId) {
    // this.processId = processId;
    // }
    //
    // public Event(String name, Long processId) {
    // this.name = name;
    // this.processId = processId;
    // }
    //
    // /**
    // * returns the simple Classname by default. Maybe overriden by subclasses to return types other than the classes
    // * name
    // */
    // public String getType() {
    // return this.getClass().getSimpleName();
    // }
    //
    // public String getName() {
    // return this.name;
    // }
    //
    // public void setName(String name) {
    // this.name = name;
    // }
    //
    // public Long getProcessId() {
    // return this.processId;
    // }
    //
    // public void setProcessId(Long processId) {
    // this.processId = processId;
    // }
    //
    // public String getOrigin() {
    // return origin;
    // }
    //
    // public void setOrigin(String origin) {
    // this.origin = origin;
    // }
    //
    // @Override
    // public String toString() {
    // try {
    // BeanInfo beanInfo = Introspector.getBeanInfo(this.getClass());
    // PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
    // StringBuilder builder = new StringBuilder();
    // builder.append("Event Properties =>");
    // for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
    // String name = propertyDescriptor.getName();
    // Object invoke = propertyDescriptor.getReadMethod().invoke(this);
    // builder.append(" " + name + ":" + invoke + ";");
    // }
    // return builder.toString();
    // } catch (IntrospectionException e) {
    // return returnSimpleEventString();
    // } catch (IllegalArgumentException e) {
    // return returnSimpleEventString();
    // } catch (IllegalAccessException e) {
    // return returnSimpleEventString();
    // } catch (InvocationTargetException e) {
    // return returnSimpleEventString();
    // }
    // }
    //
    // private String returnSimpleEventString() {
    // return "Event Properties => class:" + this.getClass().toString();
    // }
}
