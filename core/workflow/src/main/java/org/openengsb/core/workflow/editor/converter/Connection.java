package org.openengsb.core.workflow.editor.converter;

import javax.xml.bind.annotation.XmlAttribute;

public class Connection {

    @XmlAttribute
    private int to;
    @XmlAttribute
    private int from;

    public Connection() {
    }

    public Connection(int from, int to) {
        super();
        this.from = from;
        this.to = to;
    }
}
