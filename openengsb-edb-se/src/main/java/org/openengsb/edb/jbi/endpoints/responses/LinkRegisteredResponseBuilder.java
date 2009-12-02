package org.openengsb.edb.jbi.endpoints.responses;

public class LinkRegisteredResponseBuilder implements EDBEndpointResponseBuilder {
    public String wrapIntoResponse(String body) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><LinkRegistered><body>" + body + "</body></LinkRegistered>";
    }

}
