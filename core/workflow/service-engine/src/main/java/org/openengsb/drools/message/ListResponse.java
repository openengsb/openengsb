package org.openengsb.drools.message;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ListResponse {
    private Collection<String> list;

    public ListResponse() {
    }

    public ListResponse(Collection<String> list) {
        super();
        this.list = list;
    }

    public Collection<String> getList() {
        return this.list;
    }

    public void setList(Collection<String> list) {
        this.list = list;
    }
}
