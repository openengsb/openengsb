/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.core.endpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.contextcommon.ContextHelperImpl;
import org.openengsb.core.EventHelper;
import org.openengsb.core.EventHelperImpl;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.OpenEngSBComponent;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public class OpenEngSBEndpoint extends ProviderEndpoint {
    private Log log = LogFactory.getLog(getClass());

    private HashMap<String, HashMap<String, String>> contextProperties;
    private ContextHelperImpl contextHelper = new ContextHelperImpl(this, null);

    public OpenEngSBEndpoint() {
    }

    public OpenEngSBEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
    }

    public OpenEngSBEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
        super(serviceUnit, service, endpoint);
    }

    // extend the visibility of this method from protected to public
    @Override
    public void sendSync(MessageExchange me) throws MessagingException {
        super.sendSync(me);
    }

    // extend the visibility of this method from protected to public
    @Override
    public void send(MessageExchange me) throws MessagingException {
        super.send(me);
    }

    protected MethodCall toMethodCall(Source source) throws SerializationException, TransformerException {
        return Transformer.toMethodCall(new SourceTransformer().toString(source));
    }

    protected ReturnValue toReturnValue(Source source) throws SerializationException, TransformerException {
        return Transformer.toReturnValue(new SourceTransformer().toString(source));
    }

    protected Source toSource(ReturnValue returnValue) throws SerializationException {
        return new StringSource(Transformer.toXml(returnValue));
    }

    protected Source toSource(MethodCall methodCall) throws SerializationException {
        return new StringSource(Transformer.toXml(methodCall));
    }

    protected String getContextId(NormalizedMessage in) {
        return (String) in.getProperty("contextId");
    }

    protected String getCorrelationId(NormalizedMessage in) {
        String correlationId = (String) in.getProperty("correlationId");
        if (correlationId == null) {
            return UUID.randomUUID().toString();
        }
        return correlationId;
    }

    protected String getWorkflowId(NormalizedMessage in) {
        return (String) in.getProperty("workflowId");
    }

    protected String getWorkflowInstanceId(NormalizedMessage in) {
        return (String) in.getProperty("workflowInstanceId");
    }

    protected MessageProperties readProperties(NormalizedMessage in) {
        return new MessageProperties(getContextId(in), getCorrelationId(in), getWorkflowId(in),
                getWorkflowInstanceId(in));
    }

    protected void forwardInOutMessage(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out,
            QName service) throws MessagingException {
        InOut inout = getExchangeFactory().createInOutExchange();
        inout.setService(service);
        inout.setInMessage(in);
        inout.setOperation(exchange.getOperation());

        sendSync(inout);

        NormalizedMessage outMessage = inout.getOutMessage();
        out.setContent(outMessage.getContent());
    }

    protected void forwardInOnlyMessage(MessageExchange exchange, NormalizedMessage in, QName service)
            throws MessagingException {
        InOnly inonly = getExchangeFactory().createInOnlyExchange();
        inonly.setService(service);
        inonly.setInMessage(in);
        inonly.setOperation(exchange.getOperation());

        sendSync(inonly);
    }

    public EventHelper createEventHelper(MessageProperties msgProperties) {
        return new EventHelperImpl(this, msgProperties);
    }

    @Override
    public void activate() throws Exception {
        super.activate();
        register();
    }

    @Override
    public void deactivate() throws Exception {
        unregister();
        super.deactivate();
    }

    private void register() {
        OpenEngSBComponent component = (OpenEngSBComponent) serviceUnit.getComponent();

        if (contextProperties != null && contextProperties.size() != 0) {
            registerSU();
        }

        if (component.hasContextProperties()) {
            if (component.hasNoEndpoints()) {
                registerSE(component.getContextProperties());
            } else {
                log.info("Corresponding SE already registered");
            }
        }
        component.addCustomEndpoint(this);
    }

    private void unregister() {
        if (contextProperties != null && contextProperties.size() != 0) {
            unregisterSU();
        }

        OpenEngSBComponent component = (OpenEngSBComponent) serviceUnit.getComponent();
        component.removeCustomEndpoint(this);

        if (component.hasNoEndpoints() && component.hasContextProperties()) {
            unregisterSE(component.getContextProperties());
        }
    }

    private void registerSU() {
        registerService(contextProperties, true, true);
    }

    private void registerSE(HashMap<String, HashMap<String, String>> contextProperties) {
        registerService(contextProperties, false, true);
    }

    private void unregisterSU() {
        registerService(contextProperties, true, false);
    }

    private void unregisterSE(HashMap<String, HashMap<String, String>> contextProperties) {
        registerService(contextProperties, false, false);
    }

    private void registerService(HashMap<String, HashMap<String, String>> contextProperties, boolean su,
            boolean register) {
        log.info((register ? "R" : "Unr") + "egistering " + (su ? "SU" : "corresponding SE"));
        for (String key : contextProperties.keySet()) {
            contextHelper.setContext(key);
            if (register) {
                contextHelper.store(addSource(contextProperties.get(key), su ? ("SU/" + endpoint) : "SE"));
            } else {
                contextHelper.remove(addSource(contextProperties.get(key).keySet(), su ? ("SU/" + endpoint) : "SE"));
            }
        }
    }

    private HashMap<String, String> addSource(HashMap<String, String> properties, String src) {
        HashMap<String, String> newProperties = new HashMap<String, String>(properties.size());
        for (String key : properties.keySet()) {
            int pos = key.lastIndexOf("/");
            String path = key.substring(0, pos);
            String name = key.substring(pos);

            newProperties.put(path + "/" + src + name, properties.get(key));
        }
        return newProperties;
    }

    private ArrayList<String> addSource(Set<String> keys, String src) {
        ArrayList<String> newKeys = new ArrayList<String>(keys.size());
        for (String key : keys) {
            int pos = key.lastIndexOf("/");
            String path = key.substring(0, pos);
            String name = key.substring(pos);

            newKeys.add(path + "/" + src + name);
        }
        return newKeys;
    }

    public void setContextProperties(HashMap<String, HashMap<String, String>> contextProperties) {
        this.contextProperties = contextProperties;
    }
}
