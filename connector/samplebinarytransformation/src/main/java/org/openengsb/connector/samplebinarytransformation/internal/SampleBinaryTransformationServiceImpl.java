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

package org.openengsb.connector.samplebinarytransformation.internal;

import java.io.File;
import java.util.List;

import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.domain.binarytransformation.BinaryTransformationDomain;

public class SampleBinaryTransformationServiceImpl extends AbstractOpenEngSBConnectorService implements
        BinaryTransformationDomain {

    public SampleBinaryTransformationServiceImpl() {
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    @Override
    public void register(String binaryId, File... transformationConfigs) {
        // TODO Auto-generated method stub
    }

    @Override
    public void unregister(String binaryId) {
        // TODO Auto-generated method stub
    }

    @Override
    public List<String> showAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<OpenEngSBModelEntry> convertToOpenEngSBModelEntries(String binaryId, Object object) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object convertFromOpenEngSBModelEntries(String binaryId, List<OpenEngSBModelEntry> entries) {
        // TODO Auto-generated method stub
        return null;
    }

}
