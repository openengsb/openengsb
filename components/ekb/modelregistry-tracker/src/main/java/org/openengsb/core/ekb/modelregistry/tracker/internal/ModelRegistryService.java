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

package org.openengsb.core.ekb.modelregistry.tracker.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.ekb.api.ModelGraph;
import org.openengsb.core.ekb.api.ModelRegistry;
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
    private EKBClassLoader ekbClassLoader;
    private ModelGraph graphDb;

    public ModelRegistryService(BundleContext context) {
        super(context, Bundle.ACTIVE, null);
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
        return bundle.getHeaders().get(Constants.PROVIDE_MODELS_HEADER) == null;
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
            // if this catch clause is reached, then the bundle which caused this error need to be checked. There
            // is something wrong with the setup of the bundle (e.g. double import of a library of different versions)
            LOGGER.warn("Error while loading class: '{}' in bundle: '{}'", classname, bundle.getSymbolicName());
            LOGGER.debug("Exact error: ", e);
            return false;
        }
        return OpenEngSBModel.class.isAssignableFrom(clazz);
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
