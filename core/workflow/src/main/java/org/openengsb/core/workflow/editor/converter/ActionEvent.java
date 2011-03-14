package org.openengsb.core.workflow.editor.converter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class ActionEvent {

    @XmlAttribute
    private int id;
    @XmlAttribute
    private String name;

    @XmlElementWrapper(name = "eventFilters")
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

    public static class EventFilter {
        @XmlAttribute
        private String type = "eventType";
        @XmlAttribute
        private String eventType;
    }
}
