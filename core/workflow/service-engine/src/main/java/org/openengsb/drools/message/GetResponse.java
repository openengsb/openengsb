package org.openengsb.drools.message;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetResponse {
    private String name;
    private String code;

    public GetResponse() {
    }

    public GetResponse(String name, String code) {
        super();
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
