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

package org.openengsb.core.ekb.persistence.persist.edb.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.reflect.FieldUtils;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.annotation.OpenEngSBForeignKey;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.common.EDBConverter;

/**
 * The EOModel class is a helper class which encapsulates functions for models which are Engineering Objects.
 */
@SuppressWarnings("serial")
public class EOModel extends SimpleModel {
    private ModelRegistry modelRegistry;
    private EDBConverter edbConverter;

    public EOModel(OpenEngSBModel model, ModelRegistry modelRegistry, EngineeringDatabaseService edbService,
            EDBConverter edbConverter) {
        super(model, edbService);
        this.modelRegistry = modelRegistry;
        this.edbConverter = edbConverter;
    }

    /**
     * Returns a list of foreign key fields for the Engineering Object model.
     */
    public List<Field> getForeignKeyFields() {
        List<Field> fields = new ArrayList<Field>();
        for (Field field : model.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(OpenEngSBForeignKey.class)) {
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * Loads the model referenced by the given field for the given model instance. Returns null if the field has no
     * value set.
     */
    public OpenEngSBModel loadReferencedModel(Field field) {
        try {
            ModelDescription description = getModelDescriptionFromField(field);
            String modelKey = (String) FieldUtils.readField(field, model, true);
            if (modelKey == null) {
                return null;
            }
            Class<?> sourceClass = modelRegistry.loadModel(description);
            return (OpenEngSBModel) edbConverter.convertEDBObjectToModel(sourceClass, edbService.getObject(modelKey));
        } catch (SecurityException e) {
            throw new EKBException(generateErrorMessage(field), e);
        } catch (IllegalArgumentException e) {
            throw new EKBException(generateErrorMessage(field), e);
        } catch (IllegalAccessException e) {
            throw new EKBException(generateErrorMessage(field), e);
        } catch (ClassNotFoundException e) {
            throw new EKBException(generateErrorMessage(field), e);
        }
    }

    /**
     * Generates the error message for the failure of loading a model reference.
     */
    private String generateErrorMessage(Field field) {
        return String.format("Unable to load the model referenced through the field %s", field.getName());
    }

    /**
     * Generates the model description of a field which is annotated with the OpenEngSBForeignKey annotation.
     */
    private ModelDescription getModelDescriptionFromField(Field field) {
        OpenEngSBForeignKey key = field.getAnnotation(OpenEngSBForeignKey.class);
        ModelDescription description = new ModelDescription(key.modelType(), key.modelVersion());
        return description;
    }

}
