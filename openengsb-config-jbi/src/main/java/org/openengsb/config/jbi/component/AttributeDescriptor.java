/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.config.jbi.component;

public class AttributeDescriptor {
    public static enum Type {
        BOOLEAN, STRING, QNAME
    }

    private final String name;
    private final Type type;

    public AttributeDescriptor(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public AttributeDescriptor(String name, String type) {
        this.name = name;
        if (type.endsWith("boolean")) {
            this.type = Type.BOOLEAN;
        } else if (type.endsWith("QName")) {
            this.type = Type.QNAME;
        } else {
            this.type = Type.STRING;
        }
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
