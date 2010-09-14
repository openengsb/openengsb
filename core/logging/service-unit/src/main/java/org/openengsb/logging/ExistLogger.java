/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.logging;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.listener.MessageExchangeListener;
import org.openengsb.util.exist.LoggingHelper;

public class ExistLogger implements MessageExchangeListener {

    private Log log = LogFactory.getLog(getClass());
    private LoggingHelper loggingHelper;

    public void setLoggingHelper(LoggingHelper loggingHelper) {
        this.loggingHelper = loggingHelper;
    }

    public void onMessageExchange(final MessageExchange exchange) throws MessagingException {
        try {
            this.loggingHelper.log(exchange.getMessage("in"));
        } catch (Exception e) {
            this.log.error("Not possible to log message", e);
        }
    }

}
