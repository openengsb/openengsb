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

package org.openengsb.connector.userprojects.ldap.internal.ldap;

import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.connector.userprojects.ldap.internal.LdapRuntimeException;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Attribute;
import org.openengsb.domain.userprojects.model.Credential;
import org.openengsb.domain.userprojects.model.Permission;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;

/**
 * This class provides methods to build various DN's found in the DIT specific for the userprojects domain.
 * */
public final class DnFactory {

    public static final String OU_ASSIGNMENTS = "assignments";
    public static final String OU_ATTRIBUTES = "attributes";
    public static final String OU_CREDENTIALS = "credentials";
    public static final String OU_PERMISSIONS = "permissions";
    public static final String OU_PROJECTS = "projects";
    public static final String OU_ROLES = "roles";
    public static final String OU_USERS = "users";

    private DnFactory() {
    }

    public static Dn baseDn() {
        try {
            return new Dn(SchemaConstants.BASE_DN);
        } catch (LdapInvalidDnException e) {
            throw new LdapRuntimeException(e);
        }
    }

    public static Dn assignments() {
        return Utils.concatDn(SchemaConstants.OU_ATTRIBUTE, OU_ASSIGNMENTS, baseDn());
    }

    public static Dn permissions() {
        return Utils.concatDn(SchemaConstants.OU_ATTRIBUTE, OU_PERMISSIONS, baseDn());
    }

    public static Dn projects() {
        return Utils.concatDn(SchemaConstants.OU_ATTRIBUTE, OU_PROJECTS, baseDn());
    }

    public static Dn roles() {
        return Utils.concatDn(SchemaConstants.OU_ATTRIBUTE, OU_ROLES, baseDn());
    }

    public static Dn users() {
        return Utils.concatDn(SchemaConstants.OU_ATTRIBUTE, OU_USERS, baseDn());
    }

    public static Dn assignment(Assignment assignment) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, assignment.toString(), assignments());
    }

    public static Dn assignmentProject(Assignment assignment) {
        return assignmentChild(assignment, "project");
    }

    public static Dn assignmentChild(Assignment assignment, String childCn) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, childCn, assignment(assignment));
    }

    public static Dn assignmentUser(Assignment assignment) {
        return assignmentChild(assignment, "user");
    }

    public static Dn assignmentPermissions(Assignment assignment) {
        return Utils.concatDn(SchemaConstants.OU_ATTRIBUTE, OU_PERMISSIONS, assignment(assignment));
    }

    public static Dn assignmentPermission(Assignment assignment, String permission) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, permission, assignmentPermissions(assignment));
    }

    public static Dn assignmentRoles(Assignment assignment) {
        return Utils.concatDn(SchemaConstants.OU_ATTRIBUTE, OU_ROLES, assignment(assignment));
    }

    public static Dn assignmentRole(Assignment assignment, String role) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, role, assignmentRoles(assignment));
    }

    public static Dn permission(Permission permission) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, permission.toString(), permissions());
    }

    public static Dn permissionChild(Permission permission, String childCn) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, childCn, permission(permission));
    }

    public static Dn permissionComponent(Permission permission) {
        return permissionChild(permission, "component");
    }

    public static Dn permissionAction(Permission permission) {
        return permissionChild(permission, "action");
    }

    public static Dn project(Project project) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, project.getName(), projects());
    }

    public static Dn projectAttributes(Project project) {
        return Utils.concatDn(SchemaConstants.OU_ATTRIBUTE, OU_ATTRIBUTES, project(project));
    }

    public static Dn projectAttribute(Project project, Attribute attribute) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, attribute.getAttributeName(),
                projectAttributes(project));
    }

    public static Dn role(Role role) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, role.getName(), roles());
    }

    public static Dn rolePermissions(Role role) {
        return Utils.concatDn(SchemaConstants.OU_ATTRIBUTE, OU_PERMISSIONS, role(role));
    }

    public static Dn rolePermission(Role role, String permission) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, permission, rolePermissions(role));
    }

    public static Dn roleSubroles(Role role) {
        return Utils.concatDn(SchemaConstants.OU_ATTRIBUTE, OU_ROLES, role(role));
    }

    public static Dn roleSubrole(Role role, String subrole) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, subrole, roleSubroles(role));
    }

    public static Dn user(User user) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, user.getUsername(), users());
    }

    public static Dn userAttributes(User user) {
        return Utils.concatDn(SchemaConstants.OU_ATTRIBUTE, OU_ATTRIBUTES, user(user));
    }

    public static Dn userAttribute(User user, Attribute attribute) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, attribute.getAttributeName(), userAttributes(user));
    }

    public static Dn userCredentials(User user) {
        return Utils.concatDn(SchemaConstants.OU_ATTRIBUTE, OU_CREDENTIALS, user(user));
    }

    public static Dn userCredential(User user, Credential credential) {
        return Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, credential.getType(), userCredentials(user));
    }

}
