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

package org.openengsb.core.common.model;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelWrapper;
import org.openengsb.core.common.util.ModelUtils;

public final class ModelConverterStep implements ModelEntryConverterStep {
    private static ModelConverterStep instance;

    public static ModelConverterStep getInstance() {
        if (instance == null) {
            instance = new ModelConverterStep();
        }
        return instance;
    }

    private ModelConverterStep() {
    }

    @Override
    public boolean matchForGetModelEntries(Object object) {
        return object != null && OpenEngSBModel.class.isAssignableFrom(object.getClass());
    }

    @Override
    public Object convertForGetModelEntries(Object object) {
        OpenEngSBModel model = (OpenEngSBModel) object;
        return ModelUtils.generateWrapperOutOfModel(model);
    }

    @Override
    public boolean matchForGetter(Object object) {
        return object != null && object.getClass().equals(OpenEngSBModelWrapper.class);
    }

    @Override
    public Object convertForGetter(Object object) {
        OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) object;
        return ModelUtils.generateModelOutOfWrapper(wrapper);
    }

}
