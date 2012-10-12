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

package org.openengsb.connector.virtual.filewatcher;

import java.util.Collections;
import java.util.Map;

import org.openengsb.connector.virtual.filewatcher.internal.FileWatcherConnector;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.common.VirtualConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class FileWatcherConnectorFactory extends VirtualConnectorFactory<FileWatcherConnector> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcherConnectorFactory.class);

    private OsgiUtilsService utilsService;

    public FileWatcherConnectorFactory(DomainProvider domainProvider, OsgiUtilsService utilsService) {
        super(domainProvider);
        this.utilsService = utilsService;
    }

    @Override
    protected FileWatcherConnector updateHandlerAttributes(FileWatcherConnector handler,
            Map<String, String> attributes) {
        handler.setWatchfile(attributes.get("watchfile"));
        return handler;
    }

    @Override
    protected FileWatcherConnector createNewHandler(String id) {
        return new FileWatcherConnector(id);
    }

    @Override
    public Map<String, String> getValidationErrors(Map<String, String> attributes) {
        Map<String, String> result = Maps.newHashMap();
        return result;
    }

    @Override
    public Map<String, String> getValidationErrors(Connector instance, Map<String, String> attributes) {
        // TODO OPENENGSB-1290: implement some validation
        return Collections.emptyMap();
    }

}
