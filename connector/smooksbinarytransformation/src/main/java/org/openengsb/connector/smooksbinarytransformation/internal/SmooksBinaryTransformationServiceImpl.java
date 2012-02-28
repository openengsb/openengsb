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

package org.openengsb.connector.smooksbinarytransformation.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.domain.binarytransformation.BinaryTransformationDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmooksBinaryTransformationServiceImpl extends AbstractOpenEngSBConnectorService implements
        BinaryTransformationDomain {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmooksBinaryTransformationServiceImpl.class);
    private Map<String, SmooksBinaryConverter> converter;

    public SmooksBinaryTransformationServiceImpl() {
        converter = new HashMap<String, SmooksBinaryConverter>();
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    @Override
    public void register(String binaryId, Class clasz, File... transformationConfigs) {
        SmooksBinaryConverter conv = new SmooksBinaryConverter(clasz, transformationConfigs);
        converter.put(binaryId, conv);
        LOGGER.info("added new converter for the binary id {}", binaryId);
    }

    @Override
    public void unregister(String binaryId) {
        if (!converter.containsKey(binaryId)) {
            LOGGER.error("no converter for binary Id {} defined", binaryId);
            throw new IllegalArgumentException("no converter for this binary Id defined");
        }
        LOGGER.info("removed converter for the binary id {}", binaryId);
        converter.remove(binaryId);
    }

    @Override
    public List<String> showAll() {
        List<String> svs = new ArrayList<String>();
        for (Map.Entry<String, SmooksBinaryConverter> entry : converter.entrySet()) {
            svs.add(entry.getKey() + "=" + entry.getValue().toString());
        }
        return svs;
    }

    @Override
    public List<OpenEngSBModelEntry> convertToOpenEngSBModelEntries(String binaryId, Object object) {
        if (!converter.containsKey(binaryId)) {
            LOGGER.error("no converter for binary Id {} defined", binaryId);
            throw new IllegalArgumentException("no converter for this binary Id defined");
        }
        LOGGER.debug("forwarding converting request to converter");
        return converter.get(binaryId).convertToOpenEngSBModelEntries(object);
    }

    @Override
    public Object convertFromOpenEngSBModelEntries(String binaryId, List<OpenEngSBModelEntry> entries) {
        if (!converter.containsKey(binaryId)) {
            LOGGER.error("no converter for binary Id {} defined", binaryId);
            throw new IllegalArgumentException("no converter for this binary Id defined");
        }
        LOGGER.debug("forwarding converting request to transconverterformer");
        return converter.get(binaryId).convertFromOpenEngSBModelEntries(entries);
    }

    @Override
    public void openXLinks(List<OpenEngSBModel> modelObjects, String viewId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
