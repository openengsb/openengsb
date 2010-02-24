package org.openengsb.edb.jbi.service;

import java.util.ArrayList;
import java.util.Collection;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.LinkingEndpoint;
import org.openengsb.drools.NotificationDomain;
import org.openengsb.drools.model.Notification;

public class NotificationMockEndpoint extends LinkingEndpoint<NotificationDomain> {

    private Collection<Notification> receivedNotifications = new ArrayList<Notification>();

    public class DummyNotifier implements NotificationDomain {
        @Override
        public void notify(Notification notification) {
            receivedNotifications.add(notification);
        }
    }

    @Override
    protected NotificationDomain getImplementation(ContextHelper contextHelper, MessageProperties msgProperties) {
        // TODO Auto-generated method stub
        return new DummyNotifier();
    }

    public final Collection<Notification> getReceivedNotifications() {
        return this.receivedNotifications;
    }

}
