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

package org.openengsb.core.ekb.persistence.persist.edb;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang.reflect.FieldUtils;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.core.util.ModelUtils;

/**
 * This is a simple test implementation for the transformation engine which only writes attributes from the source model
 * which have the same name like one in the target model to this target attribute.
 */
public class TestTransformationEngine implements TransformationEngine {

    @Override
    public void deleteDescription(TransformationDescription arg0) {
    }

    @Override
    public void deleteDescriptionsByFile(String arg0) {
    }

    @Override
    public List<TransformationDescription> getDescriptionsByFile(String arg0) {
        return null;
    }

    @Override
    public Boolean isTransformationPossible(ModelDescription arg0, ModelDescription arg1) {
        return null;
    }

    @Override
    public Boolean isTransformationPossible(ModelDescription arg0, ModelDescription arg1, List<String> arg2) {
        return null;
    }

    @Override
    public Object performTransformation(ModelDescription arg0, ModelDescription arg1, Object arg2) {
        return null;
    }

    @Override
    public Object performTransformation(ModelDescription sourceClass, ModelDescription targetClass, Object source,
            Object target) {
        TestModelRegistry registry = new TestModelRegistry();
        try {
            Class<?> clazz = registry.loadModel(sourceClass);
            for (Field field : clazz.getDeclaredFields()) {
                String fieldName = field.getName();
                if (fieldName.equals(ModelUtils.MODEL_TAIL_FIELD_NAME) ||
                        fieldName.contains("LOGGER")) {
                    continue;
                }
                Object value = FieldUtils.readDeclaredField(source, fieldName, true);
                if (value != null) {
                    FieldUtils.writeDeclaredField(target, fieldName, value, true);
                }
            }
            return target;
        } catch (Exception e) {
            throw new EKBException("Unable to perform test transformation", e);
        }
    }

    @Override
    public Object performTransformation(ModelDescription arg0, ModelDescription arg1, Object arg2, List<String> arg3) {
        return null;
    }

    @Override
    public Object performTransformation(ModelDescription arg0, ModelDescription arg1, Object arg2, Object arg3,
            List<String> arg4) {
        return null;
    }

    @Override
    public void saveDescription(TransformationDescription arg0) {
    }

    @Override
    public void saveDescriptions(List<TransformationDescription> arg0) {
    }
}
