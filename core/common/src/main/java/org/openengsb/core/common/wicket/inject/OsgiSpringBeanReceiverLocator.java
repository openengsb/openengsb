package org.openengsb.core.common.wicket.inject;

import org.apache.wicket.IClusterable;

public interface OsgiSpringBeanReceiverLocator extends IClusterable {

    OsgiSpringBeanReceiver getSpringBeanReceiver();

}
