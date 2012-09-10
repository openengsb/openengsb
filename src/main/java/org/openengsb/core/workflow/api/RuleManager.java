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

package org.openengsb.core.workflow.api;

import java.util.Collection;
import java.util.Map;

import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.workflow.api.model.RuleBaseElementId;
import org.openengsb.core.workflow.api.model.RuleBaseElementType;

/**
 * The RuleManager serves as an abstraction to modify the rulebase used by the workflow engine. It offers methods to
 * modify the rulebase and makes sure that the changes are propagated to the WorkflowService. The elements in a rulebase
 * are identified using the {@link RuleBaseElementId} class.
 *
 * Declaration of imports and global variables are handled explicitly. Also they are the same in all packages, so they
 * are only saved once. The assignment of values to the globals is handled by the WorkflowService.
 *
 * Note: When adding a new rule or function to the rulebase, make sure that all imports are present before. Otherwise
 * the adding of the elements will fail.
 */
public interface RuleManager extends OpenEngSBService {

    /**
     *
     * adds a new element to the rulebase
     *
     * @throws RuleBaseException if adding the new element would cause the rulebase to be invalid (e.g. parse error).
     */
    void add(RuleBaseElementId name, String code) throws RuleBaseException;

    /**
     * add a new Element to the rulebase. If it already exists, the element is updated
     *
     * @throws RuleBaseException
     */
    void addOrUpdate(RuleBaseElementId name, String code) throws RuleBaseException;

    /**
     * retrieves the value for the given id
     */
    String get(RuleBaseElementId name);

    /**
     * updates an existing element
     *
     * @throws RuleBaseException if the update would cause the rulebase to be invalid (e.g. parse error).
     */
    void update(RuleBaseElementId name, String newCode) throws RuleBaseException;

    /**
     *
     * @throws RuleBaseException if the deleting the element would cause the rulebase to be invalid (e.g. parse error).
     */
    void delete(RuleBaseElementId name) throws RuleBaseException;

    /**
     * returns a list of all elements of the given type, from all packages
     */
    Collection<RuleBaseElementId> listAll(RuleBaseElementType type);

    /**
     * returns a list of all elements of the given type, for a specific package
     */
    Collection<RuleBaseElementId> list(RuleBaseElementType type, String packageName);

    /**
     * adds an import-statement used in all packages
     *
     * @throws RuleBaseException when the import causes the rulebase to become invalid (e.g. when the class cannot be
     *         found)
     */
    void addImport(String className) throws RuleBaseException;

    /**
     * removes an import-statement
     *
     * @throws RuleBaseException when removing the import causes the rulebase to become invalid (e.g. when the import is
     *         still required at some point)
     */
    void removeImport(String className) throws RuleBaseException;

    /**
     * retrieves a list of all imports. Note that all packages share the same list of imports.
     */
    Collection<String> listImports();

    /**
     *
     * @throws RuleBaseException if a global with the name already exists
     */
    void addGlobal(String className, String name) throws RuleBaseException;

    /**
     * adds a global if it is not present. If it is, this method does nothing.
     *
     * @throws RuleBaseException
     */
    void addGlobalIfNotPresent(String className, String name) throws RuleBaseException;

    /**
     * returns the typeName of the global identified the the given name
     */
    String getGlobalType(String name);

    /**
     * returns all globals of the given type (classname)
     */
    Collection<String> getAllGlobalsOfType(String type);

    /**
     * @throws RuleBaseException if removing the global would leave the rulebase in an inconsistent state (because it is
     *         still used in some rules)
     */
    void removeGlobal(String name) throws RuleBaseException;

    /**
     * @return list all globals with the name as key and the classname as value
     */
    Map<String, String> listGlobals();

}
