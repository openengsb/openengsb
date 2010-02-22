package org.openengsb.edb.jbi.endpoints;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.EventHelper;
import org.openengsb.core.EventHelperImpl;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.SimpleEventEndpoint;
import org.openengsb.core.model.Event;


/**
 * @org.apache.xbean.XBean element="edbEvent" The Endpoint to the commit-feature
 *
 */
public class EdbEventEndpoint extends SimpleEventEndpoint {

    @Override
    protected void handleEvent(Event e, ContextHelper contextHelper, MessageProperties msgProperties) {
        EventHelper helper = new EventHelperImpl(this, msgProperties);
        // simply forward to drools
        helper.sendEvent(e, "urn:openengsb:drools", "droolsService");
    }

}
