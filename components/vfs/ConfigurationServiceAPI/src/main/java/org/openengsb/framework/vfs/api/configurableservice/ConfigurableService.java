/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.openengsb.framework.vfs.api.configurableservice;

import java.util.List;
import org.openengsb.framework.vfs.api.exceptions.ReconfigurationException;

/**
 * A ConfigurableService is a service that can be reconfigured using VFS. Every
 * service that wants to be configured using VFS must implement this interface.
 */
public interface ConfigurableService {

    /**
     * Reconfigure the service.
     *
     * @throws ReconfigurationException when an failure occurs during
     * reconfiguration this exception will be thrown.
     */
    void reconfigure() throws ReconfigurationException;

    /**
     * Returns a list of the paths of the configurations that the service needs.
     */
    List<String> getPropertyList();
}
