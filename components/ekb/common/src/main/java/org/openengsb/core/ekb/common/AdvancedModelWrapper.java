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

package org.openengsb.core.ekb.common;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.ModelWrapper;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.annotation.OpenEngSBForeignKey;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;

/**
 * The AdvancedModelWrapper class is a helper class which encapsulates functions for models which are not part of the
 * standard function set.
 */
@SuppressWarnings("serial")
public class AdvancedModelWrapper extends ModelWrapper {

    protected AdvancedModelWrapper(OpenEngSBModel model) {
        super(model);
    }

    /**
     * Creates an advanced model wrapper object out of the given model object. Throws IllegalArgumentException in case
     * the given model object is no model.
     */
    public static AdvancedModelWrapper wrap(Object model) {
        if (!(isModel(model.getClass()))) {
            throw new IllegalArgumentException("The given object is no model");
        }
        return new AdvancedModelWrapper((OpenEngSBModel) model);
    }

    /**
     * Returns a list of EDBObjects which are referring to this model.
     */
    public List<EDBObject> getModelsReferringToThisModel(EngineeringDatabaseService edbService) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(EDBConverterUtils.REFERENCE_PREFIX + "%", getCompleteModelOID());
        return edbService.query(params, System.currentTimeMillis());
    }

    /**
     * Returns true if the model is an engineering object, returns false if not.
     */
    public Boolean isEngineeringObject() {
        return isEngineeringObjectClass(model.getClass());
    }

    /**
     * Returns the corresponding engineering object model wrapper to the given advanced model wrapper.
     */
    public EngineeringObjectModelWrapper toEngineeringObject() {
        return EngineeringObjectModelWrapper.enhance(this);
    }

    /**
     * Returns true if the class is the class of an engineering object, returns false if not.
     */
    public static Boolean isEngineeringObjectClass(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(OpenEngSBForeignKey.class)) {
                return true;
            }
        }
        return false;
    }
}
