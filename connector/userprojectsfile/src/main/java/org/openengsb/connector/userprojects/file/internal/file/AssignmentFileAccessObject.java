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
package org.openengsb.connector.userprojects.file.internal.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openengsb.connector.userprojects.file.internal.Configuration;
import org.openengsb.domain.userprojects.model.Assignment;

/**
 * The object providing access to the assignments file.
 */
public class AssignmentFileAccessObject extends BaseFileAccessObject {

    private final File assignmentsFile;

    public AssignmentFileAccessObject() {
        assignmentsFile = Configuration.get().getAssignmentsFile();
    }

    /**
     * Finds all the available assignments.
     * 
     * @return the list of available assignments
     */
    public List<Assignment> findAllAssignments() {
        List<Assignment> list = new ArrayList<>();
        List<String> assignmentStrings;
        try {
            assignmentStrings = readLines(assignmentsFile);
        } catch (IOException e) {
            throw new FileBasedRuntimeException(e);
        }

        for (String assignmentString : assignmentStrings) {
            if (StringUtils.isNotBlank(assignmentString)) {
                String[] substrings =
                    StringUtils
                            .splitByWholeSeparator(assignmentString, Configuration.get().getAssociationSeparator());
                if (substrings.length < 2 || StringUtils.isBlank(substrings[1])) {
                    continue;
                }
                Assignment assignment = new Assignment(substrings[0], substrings[1]);
                if (substrings.length > 2) {
                    assignment.setRoles(Arrays.asList(StringUtils.splitByWholeSeparator(substrings[2], Configuration
                            .get().getValueSeparator())));
                }
                list.add(assignment);
            }
        }
        return list;
    }

}
