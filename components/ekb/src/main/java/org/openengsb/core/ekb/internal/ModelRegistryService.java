/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.ekb.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openengsb.core.api.ekb.ModelDescription;
import org.openengsb.core.api.ekb.ModelRegistry;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.ekb.internal.graph.ModelGraph;
import org.openengsb.core.ekb.internal.loader.EKBClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the model registry. It also implements a bundle listener which checks bundles for models and
 * register/unregister them.
 */
public final class ModelRegistryService implements ModelRegistry, BundleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelRegistryService.class);
    private static ModelRegistryService instance;
    private Map<Bundle, Set<ModelDescription>> cache;
    private EKBClassLoader ekbClassLoader;
    private ModelGraph graphDb;

    public static ModelRegistryService getInstance() {
        if (instance == null) {
            instance = new ModelRegistryService();
        }
        return instance;
    }

    private ModelRegistryService() {
        cache = new HashMap<Bundle, Set<ModelDescription>>();
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if (!shouldHandleEvent(event)) {
            return;
        }
        Set<ModelDescription> models = null;
        if (cache.containsKey(event.getBundle())) {
            models = cache.get(event.getBundle());
        } else {
            models = getModels(event.getBundle());
            cache.put(event.getBundle(), models);
        }
        performModelActions(event, models);
    }

    /**
     * Returns true if a bundle event should be handled by the model registry and false if not.
     */
    private Boolean shouldHandleEvent(BundleEvent event) {
        if (event.getType() != BundleEvent.STARTED && event.getType() != BundleEvent.STOPPED) {
            return false;
        }
        return true;
    }

    /**
     * Performs the action with the received models, based on the bundle event type.
     */
    private void performModelActions(BundleEvent event, Set<ModelDescription> models) {
        if (event.getType() == BundleEvent.STARTED) {
            for (ModelDescription model : models) {
                registerModel(model);
            }
        } else if (event.getType() == BundleEvent.STOPPED) {
            for (ModelDescription model : models) {
                unregisterModel(model);
            }
        }
    }

    /**
     * Check all found classes of the bundle if they are models and return a set of all found model descriptions.
     */
    private Set<ModelDescription> getModels(Bundle bundle) {
        Set<String> classes = discoverClasses(bundle);
        Set<ModelDescription> models = new HashSet<ModelDescription>();

        for (String classname : classes) {
            if (isModelClass(classname, bundle)) {
                models.add(new ModelDescription(classname, bundle.getVersion()));
            }
        }
        return models;
    }

    /**
     * Returns true if the class with the given class name contained in the given bundle is a model and false if not or
     * the class couldn't be loaded.
     */
    private boolean isModelClass(String classname, Bundle bundle) {
        Class<?> clazz;
        try {
            clazz = bundle.loadClass(classname);
        } catch (ClassNotFoundException e) {
            LOGGER.warn(String.format("Bundle could not find own class: %s", classname), e);
            return false;
        } catch (NoClassDefFoundError e) {
            // ignore since this happens if bundle have optional imports
            return false;
        }
        return OpenEngSBModel.class.isAssignableFrom(clazz);
    }

    /**
     * Searches the bundle for classes and return a set of all class names.
     */
    private Set<String> discoverClasses(Bundle bundle) {
        Enumeration<URL> classEntries = bundle.findEntries("/", "*.class", true);
        Set<String> discoveredClasses = new HashSet<String>();
        if (classEntries == null) {
            LOGGER.debug("Found no classes in the bundle {}", bundle);
            return discoveredClasses;
        }
        while (classEntries.hasMoreElements()) {
            URL classURL = classEntries.nextElement();
            String className = extractClassName(classURL);
            discoveredClasses.add(className);
        }
        return discoveredClasses;
    }

    /**
     * Converts an URL to a class into a class name
     */
    private String extractClassName(URL classURL) {
        String path = classURL.getPath();
        return path
            .replaceAll("^/", "")
            .replaceAll(".class$", "")
            .replaceAll("\\/", ".");
    }

    @Override
    public void registerModel(ModelDescription model) {
        LOGGER.debug("Added model {} to model registry", model);
        graphDb.addModel(model);
    }

    @Override
    public void unregisterModel(ModelDescription model) {
        LOGGER.debug("Removed model {} from model registry", model);
        graphDb.removeModel(model);
    }

    @Override
    public Class<?> loadModel(ModelDescription model) throws ClassNotFoundException {
        return ekbClassLoader.loadModel(model);
    }

    @Override
    public List<String> getAnnotatedFields(ModelDescription model, Class<? extends Annotation> annotationClass)
        throws ClassNotFoundException {
        Class<?> clazz = loadModel(model);
        List<String> result = new ArrayList<String>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotationClass)) {
                result.add(field.getName());
            }
        }
        return result;
    }

    public void setEkbClassLoader(EKBClassLoader ekbClassLoader) {
        this.ekbClassLoader = ekbClassLoader;
    }

    public void setGraphDb(ModelGraph graphDb) {
        this.graphDb = graphDb;
    }
}
