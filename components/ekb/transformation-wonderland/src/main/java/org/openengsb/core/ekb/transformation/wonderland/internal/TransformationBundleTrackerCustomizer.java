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
package org.openengsb.core.ekb.transformation.wonderland.internal;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.common.transformations.TransformationUtils;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformationBundleTrackerCustomizer implements BundleTrackerCustomizer<Map<URL, List<TransformationDescription>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationBundleTrackerCustomizer.class);

    private TransformationEngine transformationEngine;

    @Override
    public Map<URL, List<TransformationDescription>> addingBundle(Bundle bundle, BundleEvent bundleEvent) {
        Enumeration<URL> entries = bundle.findEntries("/", "*.transformation", true);
        Map<URL, List<TransformationDescription>> result = new HashMap<URL, List<TransformationDescription>>();
        if(entries == null){
            return null;
        }
        while(entries.hasMoreElements()){
            URL entry = entries.nextElement();
            List<TransformationDescription> descriptionList;
            try {
                descriptionList = TransformationUtils.getDescriptionsFromXMLInputStream(entry.openStream());
            } catch (IOException e) {
                LOGGER.error("error reading transformation descriptions from {}", entry, e);
                continue;
            }
            result.put(entry, descriptionList);
            transformationEngine.saveDescriptions(descriptionList);
        }
        return result;
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, Map<URL, List<TransformationDescription>> urlTransformationDescriptionMap) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent bundleEvent, Map<URL, List<TransformationDescription>> urlTransformationDescriptionMap) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setTransformationEngine(TransformationEngine transformationEngine) {
        this.transformationEngine = transformationEngine;
    }
}
