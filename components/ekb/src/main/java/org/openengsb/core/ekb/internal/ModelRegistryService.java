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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelRegistryService implements ModelRegistry, BundleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelRegistryService.class);
    private static ModelRegistryService instance;
    private Map<Bundle, Set<ModelDescription>> cache;
    private EKBClassLoader ekbClassLoader;

    private ModelRegistryService() {
        cache = new HashMap<Bundle, Set<ModelDescription>>();
    }

    public static ModelRegistryService getInstance() {
        if (instance == null) {
            instance = new ModelRegistryService();
        }
        return instance;
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if (event.getType() != BundleEvent.INSTALLED && event.getType() != BundleEvent.UNINSTALLED) {
            return;
        }
        System.out.println("get models for bundle " + event.getBundle() + ":");
        for (ModelDescription model : getModels(event.getBundle())) {
            System.out.println(model);
        }
        Set<ModelDescription> models = null;
        if (cache.containsKey(event.getBundle())) {
            models = cache.get(event.getBundle());
        } else {
            models = getModels(event.getBundle());
        }
        performModelActions(event, models);
    }

    private void performModelActions(BundleEvent event, Set<ModelDescription> models) {
        if (event.getType() == BundleEvent.INSTALLED) {
            for (ModelDescription model : models) {
                registerModel(model);
            }
        } else if (event.getType() == BundleEvent.UNINSTALLED) {
            for (ModelDescription model : models) {
                unregisterModel(model);
            }
        }
    }

    private Set<ModelDescription> getModels(Bundle bundle) {
        Set<String> classes = discoverClasses(bundle);
        Set<ModelDescription> models = new HashSet<ModelDescription>();

        for (String classname : classes) {
            Class<?> clazz;
            try {
                clazz = bundle.loadClass(classname);
            } catch (ClassNotFoundException e) {
                LOGGER.warn("bundle could not find own class: " + classname, e);
                continue;
            }
            if (isModelClass(clazz)) {
                models.add(new ModelDescription(classname, bundle.getVersion()));
            }
        }
        return models;
    }

    private boolean isModelClass(Class<?> model) {
        return OpenEngSBModel.class.isAssignableFrom(model);
    }

    private static Set<String> discoverClasses(Bundle bundle) {
        @SuppressWarnings("unchecked")
        Enumeration<URL> classEntries = bundle.findEntries("/", "*.class", true);
        Set<String> discoveredClasses = new HashSet<String>();
        if (classEntries == null) {
            LOGGER.debug("found no classes in the bundle {}", bundle);
            return discoveredClasses;
        }
        while (classEntries.hasMoreElements()) {
            URL classURL = classEntries.nextElement();
            String className = extractClassName(classURL);
            discoveredClasses.add(className);
        }
        return discoveredClasses;
    }

    private static String extractClassName(URL classURL) {
        String path = classURL.getPath();
        return path
            .replaceAll("^/", "")
            .replaceAll(".class$", "")
            .replaceAll("\\/", ".");
    }

    @Override
    public void registerModel(ModelDescription model) {
        // TODO add model to the graph database
    }

    @Override
    public void unregisterModel(ModelDescription model) {
        // TODO remove model from the graph database
    }

    @Override
    public Class<?> loadModel(ModelDescription model) throws ClassNotFoundException {
        return ekbClassLoader.loadModel(model);
    }

    @Override
    public List<String> getAnnotatedFields(ModelDescription model, Annotation annot) throws ClassNotFoundException {
        Class<?> clazz = loadModel(model);
        List<String> result = new ArrayList<String>();
        for(Field field : clazz.getFields()) {
            if(field.isAnnotationPresent(annot.annotationType())) {
                result.add(field.getName());
            }
        }
        return result;
    }
    
    public void setEkbClassLoader(EKBClassLoader ekbClassLoader) {
        this.ekbClassLoader = ekbClassLoader;
    }
}
