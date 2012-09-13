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

package org.openengsb.core.workflow.drools.internal.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.workflow.api.RuleBaseException;
import org.openengsb.core.workflow.api.model.RuleBaseElementId;
import org.openengsb.core.workflow.api.model.RuleBaseElementType;
import org.openengsb.core.workflow.drools.internal.AbstractRuleManager;
import org.openengsb.core.workflow.drools.model.GlobalConfiguration;
import org.openengsb.core.workflow.drools.model.GlobalDeclaration;
import org.openengsb.core.workflow.drools.model.ImportConfiguration;
import org.openengsb.core.workflow.drools.model.ImportDeclaration;
import org.openengsb.core.workflow.drools.model.RuleBaseConfiguration;
import org.openengsb.core.workflow.drools.model.RuleBaseElement;

public class PersistenceRuleManager extends AbstractRuleManager {

    private ConfigPersistenceService rulePersistence;
    private ConfigPersistenceService globalPersistence;
    private ConfigPersistenceService importPersistence;

    public synchronized void init() {
        builder.reloadRulebase();
    }

    @Override
    public void add(RuleBaseElementId name, String code) throws RuleBaseException {
        try {
            List<RuleBaseConfiguration> existingRules =
                rulePersistence.load(new RuleBaseElement(name).toMetadata());
            if (!existingRules.isEmpty()) {
                throw new RuleBaseException("rule already exists");
            }
        } catch (PersistenceException e1) {
            throw new RuleBaseException("could not load existing rules from persistence service", e1);
        }

        RuleBaseElement objectToPersist = new RuleBaseElement(name, code);
        Map<String, String> metaData = objectToPersist.toMetadata();
        RuleBaseConfiguration conf = new RuleBaseConfiguration(metaData, objectToPersist);

        try {
            rulePersistence.persist(conf);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        try {
            builder.reloadPackage(name.getPackageName());
        } catch (RuleBaseException e) {
            try {
                rulePersistence.remove(metaData);
                throw e;
            } catch (PersistenceException e1) {
                throw new RuleBaseException("could not remove previously added rule, that broke the rulebase", e1);
            }
        }
    }

    @Override
    public String get(RuleBaseElementId name) {
        try {
            List<RuleBaseConfiguration> existingRules =
                rulePersistence.load(new RuleBaseElement(name).toMetadata());
            if (existingRules.isEmpty()) {
                return null;
            } else {
                return existingRules.get(0).getContent().getCode();
            }
        } catch (PersistenceException e) {
            throw new RuleBaseException("error reading rule from persistence", e);
        }
    }

    @Override
    public void update(RuleBaseElementId name, String newCode) throws RuleBaseException {
        RuleBaseElement newBean = new RuleBaseElement(name, newCode);
        RuleBaseConfiguration conf = new RuleBaseConfiguration(newBean);

        try {
            rulePersistence.persist(conf);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadPackage(name.getPackageName());
    }

    @Override
    public void delete(RuleBaseElementId name) throws RuleBaseException {
        try {
            Map<String, String> metaData = new RuleBaseElement(name).toMetadata();
            rulePersistence.remove(metaData);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadPackage(name.getPackageName());
    }

    @Override
    public Collection<RuleBaseElementId> listAll(RuleBaseElementType type) {
        RuleBaseElementId example = new RuleBaseElementId();
        example.setPackageName(null);
        example.setType(type);
        return listByExample(example);
    }

    @Override
    public Collection<RuleBaseElementId> list(RuleBaseElementType type, String packageName) {
        RuleBaseElementId example = new RuleBaseElementId();
        example.setType(type);
        example.setPackageName(packageName);
        return listByExample(example);
    }

    private Collection<RuleBaseElementId> listByExample(RuleBaseElementId example) {
        List<RuleBaseConfiguration> queryResult;
        try {
            queryResult = rulePersistence.load(new RuleBaseElement(example).toMetadata());
        } catch (PersistenceException e) {
            throw new RuleBaseException("error reading rule from persistence", e);
        }

        Collection<RuleBaseElementId> result = new HashSet<RuleBaseElementId>();
        for (RuleBaseConfiguration element : queryResult) {
            result.add(element.getContent().generateId());
        }
        return result;
    }

    @Override
    public void addImport(String className) throws RuleBaseException {
        ImportDeclaration imp = new ImportDeclaration(className);
        Map<String, String> metaData = imp.toMetadata();
        ImportConfiguration cnf = new ImportConfiguration(metaData, imp);
        try {
            if (importPersistence.load(metaData).isEmpty()) {
                importPersistence.persist(cnf);
            }
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        try {
            builder.reloadRulebase();
        } catch (RuleBaseException e) {
            importPersistence.remove(cnf.getMetaData());
            throw e;
        }
    }

    @Override
    public void removeImport(String className) throws RuleBaseException {
        try {
            ImportDeclaration imp = new ImportDeclaration(className);
            Map<String, String> metaData = imp.toMetadata();
            importPersistence.remove(metaData);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadRulebase();
    }

    @Override
    public Collection<String> listImports() {
        ImportDeclaration imp = new ImportDeclaration();
        Map<String, String> metaData = imp.toMetadata();
        List<ImportConfiguration> queryResult;
        try {
            queryResult = importPersistence.load(metaData);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        Collection<String> result = new HashSet<String>();
        for (ImportConfiguration i : queryResult) {
            result.add(i.getContent().getClassName());
        }
        return result;
    }

    @Override
    public void addGlobal(String className, String name) throws RuleBaseException {
        GlobalDeclaration globalDeclaration = new GlobalDeclaration(name);
        Map<String, String> metaData = globalDeclaration.toMetadata();
        List<GlobalConfiguration> queryResult;
        try {
            queryResult = globalPersistence.load(metaData);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        if (!queryResult.isEmpty()) {
            throw new RuleBaseException(String.format("Global with name \"%s\" already exists", name));
        }
        globalDeclaration.setClassName(className);
        GlobalConfiguration cnf = new GlobalConfiguration(metaData, globalDeclaration);
        try {
            globalPersistence.persist(cnf);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        try {
            builder.reloadRulebase();
        } catch (RuleBaseException e) {
            globalPersistence.remove(cnf.getMetaData());
            throw e;
        }

    }

    @Override
    public void removeGlobal(String name) throws RuleBaseException {
        GlobalDeclaration globalDeclaration = new GlobalDeclaration(name);
        Map<String, String> metaData = globalDeclaration.toMetadata();
        try {
            globalPersistence.remove(metaData);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadRulebase();
    }

    @Override
    public Map<String, String> listGlobals() {
        GlobalDeclaration globalDeclaration = new GlobalDeclaration();
        Map<String, String> metaData = globalDeclaration.toMetadata();
        List<GlobalConfiguration> queryResult;
        try {
            queryResult = globalPersistence.load(metaData);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        Map<String, String> globals = new HashMap<String, String>();
        for (GlobalConfiguration g : queryResult) {
            globals.put(g.getContent().getVariableName(), g.getContent().getClassName());
        }
        return globals;
    }

    public void setGlobalPersistence(ConfigPersistenceService globalPersistence) {
        this.globalPersistence = globalPersistence;
    }

    public void setImportPersistence(ConfigPersistenceService importPersistence) {
        this.importPersistence = importPersistence;
    }

    public void setRulePersistence(ConfigPersistenceService rulePersistence) {
        this.rulePersistence = rulePersistence;
    }
}
