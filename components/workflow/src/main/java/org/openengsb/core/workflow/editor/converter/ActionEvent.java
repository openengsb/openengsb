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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;

public class ActionEvent {

    private int id;
    private String name;

    private List<EventFilter> eventFilter = new ArrayList<EventFilter>();

    public ActionEvent() {
    }

    public ActionEvent(int id, String name, String eventType) {
        super();
        this.id = id;
        this.name = name;
        EventFilter e = new EventFilter();
        e.eventType = eventType;
        this.eventFilter.add(e);
    }

    public static final class EventFilter {

        private EventFilter() {
        }

        @XmlAttribute
        public String type = "eventType";
        @XmlAttribute
        public String eventType;
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlElementWrapper(name = "eventFilters")
    public List<EventFilter> getEventFilter() {
        return eventFilter;
    }

}
