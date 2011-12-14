package org.openengsb.ui.admin.testClient;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.openengsb.ui.admin.basePage.BasePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonMethodCallMarshalOutput  extends BasePage  {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestClient.class);
    private String json_messages;

    public JsonMethodCallMarshalOutput(String json_messages) {
        this.json_messages = json_messages;
        add(new MultiLineLabel("json_output", json_messages));  
    }
    
    
    
}
