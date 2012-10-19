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

package org.openengsb.core.common.transformations;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Provides some utility methods in the area of model transformations.
 */
public final class TransformationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationUtils.class);

    private TransformationUtils() {
    }

    /**
     * Scans an input stream for transformation descriptions and returns all successfully read transformation
     * descriptions.
     */
    public static List<TransformationDescription> getDescriptionsFromXMLInputStream(InputStream fileContent) {
        List<TransformationDescription> desc = new ArrayList<TransformationDescription>();
        try {
            desc = loadDescrtipionsFromXMLInputSource(new InputSource(fileContent), null);
        } catch (Exception e) {
            LOGGER.error("Unable to read the descriptions from input stream. ", e);
        }
        return desc;
    }

    /**
     * Scans a XML file for transformation descriptions and returns all successfully read transformation descriptions.
     */
    public static List<TransformationDescription> getDescriptionsFromXMLFile(File file) {
        List<TransformationDescription> desc = new ArrayList<TransformationDescription>();
        try {
            return loadDescrtipionsFromXMLInputSource(new InputSource(file.getAbsolutePath()), file.getName());
        } catch (Exception e) {
            LOGGER.error("Unable to read the descriptions from file " + file.getAbsolutePath(), e);
        }
        return desc;
    }

    /**
     * Does the actual parsing.
     */
    private static List<TransformationDescription> loadDescrtipionsFromXMLInputSource(InputSource source,
                                                                                      String fileName)
            throws Exception {
        XMLReader xr = XMLReaderFactory.createXMLReader();
        TransformationDescriptionXMLReader reader = new TransformationDescriptionXMLReader(fileName);
        xr.setContentHandler(reader);
        xr.parse(source);
        return reader.getResult();
    }

    /**
     * creates a {@link ModelDescription} of the given type using the name of the class and the version of the exported package
     * this class originates from (as returned by {@link TransformationUtils#getClassVersion(Class)})
     */
    public static ModelDescription toModelDescription(Class<?> type) {
        return new ModelDescription(type, getClassVersion(type));
    }

    /**
     * determines the version of the given class. It does so by determining the Bundle the class was loaded and then
     * looking at the export-package-headers of the corresponding package in that bundle.
     */
    public static String getClassVersion(Class<?> type) {
        Bundle bundle = FrameworkUtil.getBundle(type);
        if (bundle == null) { // it was the bootstrap-classloader
            return Version.emptyVersion.toString();
        }
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        List<BundleCapability> capabilities = wiring.getCapabilities(BundleRevision.PACKAGE_NAMESPACE);
        for (BundleCapability capability : capabilities) {
            if (capability.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE).equals(type.getPackage().getName())) {
                return (String) capability.getAttributes().get(Constants.VERSION_ATTRIBUTE);
            }
        }
        // just fallback, this shouldn't happen
        return bundle.getVersion().toString();
    }

    /**
     * tries to find a method that method in the class {@code target} with the same name and the same number of
     * arguments. It's assumed that the arguments can then be transformed.
     *
     * @throws java.util.NoSuchElementException
     *          if no matching method can be found
     */
    public static Method findTargetMethod(final Method sourceMethod, Class<?> target) throws NoSuchElementException {
        return Iterables.find(Arrays.asList(target.getMethods()), new Predicate<Method>() {
            @Override
            public boolean apply(Method element) {
                if (!sourceMethod.getName().equals(element.getName())) {
                    return false;
                }
                if (sourceMethod.getParameterTypes().length != element.getParameterTypes().length) {
                    return false;
                }
                return true;
            }
        });
    }
}
