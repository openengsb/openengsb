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

package org.openengsb.core.ekb.impl.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.impl.internal.graph.ModelGraph;
import org.openengsb.core.ekb.impl.internal.loader.EKBClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the model registry. It also implements a bundle tracker which checks bundles for models and
 * register/unregister them.
 */
public final class ModelRegistryService extends BundleTracker implements ModelRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelRegistryService.class);
    private static ModelRegistryService instance;
    private EKBClassLoader ekbClassLoader;
    private ModelGraph graphDb;
    private List<String> bundleFilter;

    public static ModelRegistryService getInstance(BundleContext context) {
        if (instance == null) {
            instance = new ModelRegistryService(context);
        }
        return instance;
    }

    private ModelRegistryService(BundleContext context) {
        super(context, Bundle.ACTIVE, null);
        // all bundles listed here made problems because of Errors (e.g. VerifyErrors and IncompatibleClassChangeErrors)
        bundleFilter = new ArrayList<String>();
        bundleFilter.add("org.apache.xbean.finder");
        bundleFilter.add("org.ops4j.pax.url.mvn");
        bundleFilter.add("org.eclipse.jetty.aggregate.jetty-all-server");
        bundleFilter.add("org.apache.cxf.bundle");
        bundleFilter.add("PAXEXAM-PROBE");
        bundleFilter.add("wrap_mvn_junit_junit");
        bundleFilter.add("org.apache.servicemix.bundles.jaxb-xjc");
        bundleFilter.add("org.ops4j.pax.web.pax-web-extender-whiteboard");
        bundleFilter.add("org.ops4j.pax.web.pax-web-extender-war");
        bundleFilter.add("org.apache.shiro.core");
        bundleFilter.add("org.apache.shiro.web");
        bundleFilter.add("org.apache.shiro.spring");
    }

    @Override
    public Object addingBundle(Bundle bundle, BundleEvent event) {
        Set<ModelDescription> models = scanBundleForModels(bundle);
        for (ModelDescription model : models) {
            registerModel(model);
            LOGGER.info("Registered model: {}", model);
        }
        return models;
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
        @SuppressWarnings("unchecked")
        Set<ModelDescription> models = (Set<ModelDescription>) object;
        for (ModelDescription model : models) {
            unregisterModel(model);
            LOGGER.info("Unregistered model: {}", model);
        }
    }

    /**
     * Check all found classes of the bundle if they are models and return a set of all found model descriptions.
     */
    private Set<ModelDescription> scanBundleForModels(Bundle bundle) {
        Set<ModelDescription> models = new HashSet<ModelDescription>();
        if (!shouldSkipBundle(bundle)) {
            models = loadModelsOfBundle(bundle);
        }
        return models;
    }

    /**
     * Returns true if the given bundle should be skipped in the model search process. Returns false otherwise.
     */
    private boolean shouldSkipBundle(Bundle bundle) {
        for (String filter : bundleFilter) {
            if (bundle.getSymbolicName().contains(filter)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Searches the bundle for model classes and return a set of them.
     */
    private Set<ModelDescription> loadModelsOfBundle(Bundle bundle) {
        Enumeration<URL> classEntries = bundle.findEntries("/", "*.class", true);
        Set<ModelDescription> models = new HashSet<ModelDescription>();
        if (classEntries == null) {
            LOGGER.debug("Found no classes in the bundle {}", bundle);
            return models;
        }
        while (classEntries.hasMoreElements()) {
            URL classURL = classEntries.nextElement();
            String classname = extractClassName(classURL);
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
        LOGGER.debug("Check if class '{}' is a model class", classname);
        Class<?> clazz;
        try {
            clazz = bundle.loadClass(classname);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Bundle could not load its own class: '{}' bundle: '{}'", classname, bundle.getSymbolicName());
            LOGGER.debug("Exact error: ", e);
            return false;
        } catch (NoClassDefFoundError e) {
            // ignore since this happens if bundle have optional imports
            return false;
        } catch (Error e) {
            // there are some bundles where this catch clause is needed. Some bundles throw errors like VerifyErrors
            // and IncompatibleClassChangeErrors when trying to load a class. All classes which where found to throw
            // such errors, were put in the bundleFilter list.
            LOGGER.warn("Error while loading class: '{}' in bundle: '{}'", classname, bundle.getSymbolicName());
            LOGGER.debug("Exact error: ", e);
            return false;
        }
        return clazz.isAnnotationPresent(Model.class);
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
