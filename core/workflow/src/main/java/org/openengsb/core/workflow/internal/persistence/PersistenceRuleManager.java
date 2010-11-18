/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openengsb.core.workflow.internal.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.common.workflow.RuleBaseException;
import org.openengsb.core.common.workflow.model.RuleBaseElementId;
import org.openengsb.core.common.workflow.model.RuleBaseElementType;
import org.openengsb.core.workflow.internal.AbstractRuleManager;

public class PersistenceRuleManager extends AbstractRuleManager {

    private PersistenceService persistence;

    @Override
    public void add(RuleBaseElementId name, String code) throws RuleBaseException {
        List<RuleBaseElement> existingRules = persistence.query(new RuleBaseElement(name));
        if (!existingRules.isEmpty()) {
            throw new RuleBaseException("rule already exists");
        }
        RuleBaseElement objectToPersist = new RuleBaseElement(name, code);
        try {
            persistence.create(objectToPersist);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        try {
            builder.reloadPackage(name.getPackageName());
        } catch (RuleBaseException e) {
            try {
                persistence.delete(objectToPersist);
                throw e;
            } catch (PersistenceException e1) {
                throw new RuntimeException("could not remove previously added rule, that broke the rulebase", e1);
            }
        }
    }

    @Override
    public String get(RuleBaseElementId name) {
        List<RuleBaseElement> query = persistence.query(new RuleBaseElement(name));
        if (query.size() != 1) {
            return null;
        }
        return query.get(0).getCode();
    }

    @Override
    public void update(RuleBaseElementId name, String newCode) throws RuleBaseException {
        RuleBaseElement oldBean = new RuleBaseElement(name);
        RuleBaseElement newBean = new RuleBaseElement(name, newCode);
        try {
            persistence.update(oldBean, newBean);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadPackage(name.getPackageName());
    }

    @Override
    public void delete(RuleBaseElementId name) throws RuleBaseException {
        try {
            persistence.delete(new RuleBaseElement(name));
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadPackage(name.getPackageName());
    }

    @Override
    public Collection<RuleBaseElementId> list(RuleBaseElementType type) {
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
        List<RuleBaseElement> queryResult = persistence.query(new RuleBaseElement(example));
        Collection<RuleBaseElementId> result = new HashSet<RuleBaseElementId>();
        for (RuleBaseElement element : queryResult) {
            result.add(element.getId());
        }
        return result;
    }

    @Override
    public void addImport(String className) throws RuleBaseException {
        ImportDeclaration imp = new ImportDeclaration(className);
        if (persistence.query(imp).isEmpty()) {
            try {
                persistence.create(imp);
            } catch (PersistenceException e) {
                throw new RuleBaseException(e);
            }
        }
        builder.reloadRulebase();
    }

    @Override
    public void removeImport(String className) throws RuleBaseException {
        try {
            persistence.delete(new ImportDeclaration(className));
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadRulebase();
    }

    @Override
    public Collection<String> listImports() {
        List<ImportDeclaration> queryResult = persistence.query(new ImportDeclaration());
        Collection<String> result = new HashSet<String>();
        for (ImportDeclaration i : queryResult) {
            result.add(i.getClassName());
        }
        return result;
    }

    @Override
    public void addGlobal(String className, String name) throws RuleBaseException {
        GlobalDeclaration globalDeclaration = new GlobalDeclaration(name);
        if (!persistence.query(globalDeclaration).isEmpty()) {
            throw new RuleBaseException(String.format("Global with name \"%s\" already exists", name));
        }
        globalDeclaration.setClassName(className);
        try {
            persistence.create(globalDeclaration);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadRulebase();
    }

    @Override
    public void removeGlobal(String name) throws RuleBaseException {
        GlobalDeclaration globalDeclaration = new GlobalDeclaration(name);
        try {
            persistence.delete(globalDeclaration);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadRulebase();
    }

    @Override
    public Map<String, String> listGlobals() {
        List<GlobalDeclaration> query = persistence.query(new GlobalDeclaration());
        Map<String, String> globals = new HashMap<String, String>();
        for (GlobalDeclaration g : query) {
            globals.put(g.getVariableName(), g.getClassName());
        }
        return globals;
    }

    public void setPersistence(PersistenceService persistence) {
        this.persistence = persistence;
    }
}
