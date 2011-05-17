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

package org.openengsb.core.workflow.editor.validator;

import org.openengsb.core.api.workflow.WorkflowValidationResult;
import org.openengsb.core.api.workflow.WorkflowValidator;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.EventRepresentation;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;

public class WorkflowValidatorImplementation implements WorkflowValidator {

    @Override
    public WorkflowValidationResult validate(WorkflowRepresentation representation) {
        WorkflowValidationResultImplementation result = new WorkflowValidationResultImplementation();
        if (representation.getName() == "" || representation.getName() == null) {
            result.notValid();
            result.addError("Name of WorkflowRepresentation missing");
        }
        validateActionRepresentation(representation.getRoot(), result);
        return result;
    }

    private void validateActionRepresentation(ActionRepresentation representation,
            WorkflowValidationResultImplementation result) {
        for (ActionRepresentation action : representation.getActions()) {
            validateActionRepresentation(action, result);
        }
        for (EventRepresentation event : representation.getEvents()) {
            validateEventRepresentation(event, result);
        }
    }

    private void validateEventRepresentation(EventRepresentation representation,
            WorkflowValidationResultImplementation result) {
        if (representation.getActions().size() == 0 && representation.getEvents().size() == 0) {
            result.notValid();
            result.addError(representation.getEvent().getCanonicalName() + " has to have either an Action or an Event");
        }
        for (ActionRepresentation action : representation.getActions()) {
            validateActionRepresentation(action, result);
        }
        for (EventRepresentation event : representation.getEvents()) {
            validateEventRepresentation(event, result);
        }
    }
}
