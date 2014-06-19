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

package org.openengsb.framework.vfs.vfsconfigurationservice.activator;

import org.openengsb.framework.vfs.api.configurationservice.ConfigurationService;
import org.openengsb.framework.vfs.vfsconfigurationservice.vfsconfigurationservice.VFSConfigurationService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
    private final Logger logger = LoggerFactory.getLogger(Activator.class);
    private VFSConfigurationService vfsConfigurationService;
    private ConfigurationService configurationServiceRepository;

    public void start(BundleContext bc) throws Exception {
        if (vfsConfigurationService == null) {

            vfsConfigurationService = new VFSConfigurationService(bc);
            vfsConfigurationService.start();
            configurationServiceRepository = (ConfigurationService) vfsConfigurationService;
        }

        logger.debug("Register ConfigurationServiceRepository");
        bc.registerService(ConfigurationService.class.getName(), configurationServiceRepository, null);
    }

    public void stop(BundleContext bc) throws Exception {
        logger.debug("Stopping bundle VFSConfigurationService");
        if (vfsConfigurationService != null) {
            vfsConfigurationService.stop();
        }
    }
}
