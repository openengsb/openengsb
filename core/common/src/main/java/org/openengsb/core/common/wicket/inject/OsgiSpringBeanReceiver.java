package org.openengsb.core.common.wicket.inject;

import org.apache.wicket.IClusterable;

public interface OsgiSpringBeanReceiver extends IClusterable {

    Object getBean(String springBeanName, String bundleSymbolicName);

}
