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

package org.openengsb.core.workflow.editor.converter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;

public class ActionNode {

    private int id;
    private String name;

    private Action action;

    public ActionNode() {
    }

    public ActionNode(int id, String name, String location, String code) {
        super();
        this.id = id;
        this.name = name;
        this.action = new Action();
        this.action.value = code;
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlElement
    public Action getAction() {
        return action;
    }

    public static final class Action {
        @XmlAttribute
        public String type = "expression";
        @XmlAttribute
        public String dialect = "java";

        @XmlValue
        public String value;

        public void setValue(String value) {
            this.value = value;
        }
    }
}
