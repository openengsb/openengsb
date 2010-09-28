package org.openengsb.core.common.wicket.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
/**
 * Similar to Wicket's default SpringBean with the difference that the symbolic name of the bundle the bean is located
 * in is required. The injector will retrieve the spring bean from the correct bundle.
 */
public @interface OsgiSpringBean {

    /**
     * The name of the SpringBean defined in a spring context file.
     */
    String springBeanName();

    /**
     * The symbolic name of the bundle containing the spring bean definition.
     */
    String bundleSymbolicName();

}
