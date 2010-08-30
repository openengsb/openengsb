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
package org.openengsb.domains.notification.internal;

import org.openengsb.core.common.context.ContextService;
import org.openengsb.domains.notification.NotificationDomain;
import org.openengsb.domains.notification.model.Notification;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.BundleContextAware;

public class NotificationForwardDomain implements NotificationDomain, BundleContextAware {

    private BundleContext bundleContext;
    private ContextService contextService;

    @Override
    public void notify(Notification notification) {
        String id = contextService.getValue("domains/notification/defaultConnector/id");
        if (id == null) {
            throw new IllegalStateException("no default connector known for example domain");
        }
        ServiceReference[] references = null;
        try {
            references = bundleContext.getServiceReferences(NotificationDomain.class.getName(), "(id=" + id + ")");
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(e);
        }
        if (references.length != 1) {
            throw new IllegalStateException("lookup for service with id '" + id + "' returned " + references.length
                    + " services");
        }
        NotificationDomain service = (NotificationDomain) bundleContext.getService(references[0]);
        try {
            service.notify(notification);
        } finally {
            bundleContext.ungetService(references[0]);
        }
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setContextService(ContextService contextService) {
        this.contextService = contextService;
    }
}
