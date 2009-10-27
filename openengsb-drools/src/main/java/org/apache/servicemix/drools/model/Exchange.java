/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.drools.model;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.RobustInOnly;
import javax.xml.namespace.NamespaceContext;

public class Exchange {

    public static final String IN_ONLY = "InOnly";
    public static final String ROBUST_IN_ONLY = "RobustInOnly";
    public static final String IN_OUT = "InOut";
    public static final String IN_OPTIONAL_OUT = "InOptionalOut";
    
    public static final String ACTIVE = "Active";
    public static final String ERROR = "Error";
    public static final String DONE = "Done";
    
    public static final String IN_MESSAGE = "in";
    public static final String OUT_MESSAGE = "out";
    
    private final MessageExchange exchange;
    private Message in;
    private Message out;
    private Message fault;
    private NamespaceContext namespaceContext;
    
    public Exchange(MessageExchange exchange, NamespaceContext namespaceContext) {
        this.exchange = exchange;
        this.namespaceContext = namespaceContext;
        if (in == null) {
            NormalizedMessage msg = exchange.getMessage("in");
            in = msg != null ? new Message(msg, this.namespaceContext) : null;
        }
        if (out == null) {
            NormalizedMessage msg = exchange.getMessage("out");
            out = msg != null ? new Message(msg, this.namespaceContext) : null;
        }
        if (fault == null) {
            javax.jbi.messaging.Fault msg = exchange.getFault();
            fault = msg != null ? new Fault(msg, this.namespaceContext) : null;
        }
    }
    
    public MessageExchange getInternalExchange() {
        return exchange;
    }
    
    public String getMep() {
        if (exchange instanceof InOnly) {
            return IN_ONLY;
        } else if (exchange instanceof RobustInOnly) {
            return ROBUST_IN_ONLY;
        } else if (exchange instanceof InOut) {
            return IN_OUT;
        } else if (exchange instanceof InOptionalOut) {
            return IN_OPTIONAL_OUT;
        } else {
            return exchange.getPattern().toString();
        }
    }
    
    public String getStatus() {
        if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
            return ACTIVE;
        } else if (exchange.getStatus() == ExchangeStatus.DONE) {
            return DONE;
        } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
            return ERROR;
        } else {
            throw new IllegalStateException("Unkown exchange status");
        }
    }
    
    public String getOperation() {
        return exchange.getOperation() != null ? exchange.getOperation().toString() : null;
    }
    
    public Object getProperty(String name) {
        return exchange.getProperty(name);
    }
    
    public void setProperty(String name, Object value) {
        exchange.setProperty(name, value);
    }
    
    public Message getIn() {
        return in;
    }
    
    public Message getOut() {
        return out;
    }
    
    public Message getFault() {
        return fault;
    }
    
    protected Message getMessage(String name) {
        NormalizedMessage msg = exchange.getMessage(name);
        return msg != null ? new Message(msg, this.namespaceContext) : null;
    }
    
}

