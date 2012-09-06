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

package org.openengsb.core.ekb.transformation.wonderland;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.transformation.wonderland.models.ModelA;
import org.openengsb.core.ekb.transformation.wonderland.models.ModelB;
import org.openengsb.core.ekb.transformation.wonderland.models.ModelC;
import org.osgi.framework.Version;

public abstract class TransformationEngineTests {

    protected ModelDescription getModelADescription() {
        return new ModelDescription(ModelA.class, new Version(1, 0, 0));
    }

    protected ModelDescription getModelBDescription() {
        return new ModelDescription(ModelB.class, new Version(1, 0, 0));
    }

    protected ModelDescription getModelCDescription() {
        return new ModelDescription(ModelC.class, new Version(1, 0, 0));
    }
}
