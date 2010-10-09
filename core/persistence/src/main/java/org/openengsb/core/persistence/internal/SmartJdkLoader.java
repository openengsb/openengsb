package org.openengsb.core.persistence.internal;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.db4o.reflect.jdk.JdkLoader;

public class SmartJdkLoader implements JdkLoader {

    private static Log log = LogFactory.getLog(SmartJdkLoader.class);

    private final Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();

    @Override
    public Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException cnfe) {
            log.info("Class.forName() can't find: " + className);
        }
        for (ClassLoader classLoader : classLoaders) {
            try {
                return classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                log.info("LoadClass of class loader '" + classLoader + "' can't find: " + className);
            }
        }
        log.warn("Can't find class for className: " + className);
        return null;
    }

    public void addClassLoader(ClassLoader classLoader) {
        if (!classLoaders.contains(classLoader)) {
            classLoaders.add(classLoader);
        }
    }

    @Override
    public Object deepClone(Object context) {
        SmartJdkLoader myClone = new SmartJdkLoader();
        for (ClassLoader classLoader : classLoaders) {
            myClone.addClassLoader(classLoader);
        }
        return myClone;
    }

}
