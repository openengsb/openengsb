package org.openengsb.core.common.wicket.inject;

import org.apache.wicket.Application;
import org.apache.wicket.IClusterable;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.injection.ComponentInjector;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.protocol.http.WebApplication;

public class OsgiSpringComponentInjector extends ComponentInjector {

    @SuppressWarnings("serial")
    private static final MetaDataKey<SpringBeanReceiverHolder> CONTEXT_KEY = 
        new MetaDataKey<SpringBeanReceiverHolder>() { };

    public OsgiSpringComponentInjector(WebApplication webapp, OsgiSpringBeanReceiver springBeanReceiver) {
        webapp.setMetaData(CONTEXT_KEY, new SpringBeanReceiverHolder(springBeanReceiver));
        InjectorHolder.setInjector(new OsgiAnnotationSpringInjector(new SpringBeanReceiverLocatorImpl()));
    }

    private static class SpringBeanReceiverHolder implements IClusterable {
        private static final long serialVersionUID = 1L;

        private final OsgiSpringBeanReceiver springBeanReceiver;

        public SpringBeanReceiverHolder(OsgiSpringBeanReceiver springBeanReceiver) {
            this.springBeanReceiver = springBeanReceiver;
        }

        public OsgiSpringBeanReceiver getContext() {
            return springBeanReceiver;
        }
    }

    @SuppressWarnings("serial")
    private static class SpringBeanReceiverLocatorImpl implements OsgiSpringBeanReceiverLocator {

        private transient OsgiSpringBeanReceiver springBeanReceiver;

        @Override
        public OsgiSpringBeanReceiver getSpringBeanReceiver() {
            if (springBeanReceiver == null) {
                springBeanReceiver = Application.get().getMetaData(CONTEXT_KEY).getContext();
            }
            return springBeanReceiver;
        }

    }
}
