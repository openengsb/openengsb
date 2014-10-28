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
import org.openengsb.domain.userprojects.model.Role;

/**
 * The object providing access to the roles file.
 */
public class RoleFileAccessObject extends BaseFileAccessObject {

    private static final String ROLES_FILE_NAME = "roles";

    private final File rolesFile;

    public RoleFileAccessObject(File mainDir) {
        rolesFile = new File(mainDir, ROLES_FILE_NAME);
    }

    public RoleFileAccessObject(String mainDirName) {
        this(new File(mainDirName));
    }

    /**
     * Finds all the available roles.
     * 
     * @return the list of available roles
     */
    public List<Role> findAllRoles() {
        List<Role> list = new ArrayList<>();
        List<String> roleStrings;
        try {
            roleStrings = readLines(rolesFile);
        } catch (IOException e) {
            throw new FileBasedRuntimeException(e);
        }
        for (String roleString : roleStrings) {
            String[] substrings = StringUtils.split(roleString, ASSOCIATION_SEPARATOR);
            Role role = new Role(substrings[0]);
            if (substrings.length > 1) {
                role.setRoles(Arrays.asList(StringUtils.split(substrings[1], VALUE_SEPARATOR)));
            }
            list.add(role);
        }
        
        return list;
    }

}
