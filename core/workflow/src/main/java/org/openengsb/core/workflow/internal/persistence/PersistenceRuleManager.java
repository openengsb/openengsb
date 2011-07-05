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

package org.openengsb.core.workflow.internal.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.workflow.internal.AbstractRuleManager;
import org.openengsb.core.workflow.model.GlobalDeclaration;
import org.openengsb.core.workflow.model.ImportDeclaration;
import org.openengsb.core.workflow.model.RuleBaseElement;

public class PersistenceRuleManager extends AbstractRuleManager {

    private static final String META_RULE = "rule";
    private static final String META_RULE_TYPE = "type";
    private static final String META_RULE_NAME = "name";
    private static final String META_RULE_PACKAGE = "package";

    private static final String META_IMPORT = "import";
    private static final String META_IMPORT_CLASS = "class";

    private static final String META_GLOBAL = "global";
    private static final String META_GLOBAL_VARIABLE = "variable";

    private ConfigPersistenceService persistenceService;

    @Override
    public void init() throws RuleBaseException {
        if (persistenceService == null) {
            persistenceService =
                OpenEngSBCoreServices.getConfigPersistenceService("persistenceService");
        }

        super.init();

    }

    @Override
    public void add(RuleBaseElementId name, String code) throws RuleBaseException {
        try {
            List<ConfigItem<RuleBaseElement>> existingRules =
                persistenceService.load(getMetadataFromRuleBaseElement(new RuleBaseElement(name)));
            if (!existingRules.isEmpty()) {
                throw new RuleBaseException("rule already exists");
            }
        } catch (PersistenceException e1) {
            throw new RuntimeException("could not load existing rules from persistence service", e1);
        }

        RuleBaseElement objectToPersist = new RuleBaseElement(name, code);
        Map<String, String> metaData = getMetadataFromRuleBaseElement(objectToPersist);
        ConfigItem<RuleBaseElement> conf = new ConfigItem<RuleBaseElement>(metaData, objectToPersist);

        try {
            persistenceService.persist(conf);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        try {
            builder.reloadPackage(name.getPackageName());
        } catch (RuleBaseException e) {
            try {
                persistenceService.remove(metaData);
                throw e;
            } catch (PersistenceException e1) {
                throw new RuntimeException("could not remove previously added rule, that broke the rulebase", e1);
            }
        }
    }

    @Override
    public String get(RuleBaseElementId name) {
        try {
            List<ConfigItem<RuleBaseElement>> existingRules =
                persistenceService.load(getMetadataFromRuleBaseElement(new RuleBaseElement(name)));
            if (existingRules.isEmpty()) {
                return null;
            } else {
                return existingRules.get(0).getContent().getCode();
            }
        } catch (PersistenceException e) {
            throw new RuntimeException("error reading rule from persistence", e);
        }
    }

    @Override
    public void update(RuleBaseElementId name, String newCode) throws RuleBaseException {
        RuleBaseElement newBean = new RuleBaseElement(name, newCode);

        Map<String, String> metaData = getMetadataFromRuleBaseElement(newBean);
        ConfigItem<RuleBaseElement> conf = new ConfigItem<RuleBaseElement>(metaData, newBean);

        try {
            persistenceService.persist(conf);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadPackage(name.getPackageName());
    }

    @Override
    public void delete(RuleBaseElementId name) throws RuleBaseException {
        try {
            Map<String, String> metaData = getMetadataFromRuleBaseElement(new RuleBaseElement(name));
            persistenceService.remove(metaData);
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
        List<ConfigItem<RuleBaseElement>> queryResult;
        try {
            queryResult = persistenceService.load(getMetadataFromRuleBaseElement(new RuleBaseElement(example)));
        } catch (PersistenceException e) {
            throw new RuntimeException("error reading rule from persistence", e);
        }

        Collection<RuleBaseElementId> result = new HashSet<RuleBaseElementId>();
        for (ConfigItem<RuleBaseElement> element : queryResult) {
            result.add(element.getContent().generateId());
        }
        return result;
    }

    @Override
    public void addImport(String className) throws RuleBaseException {
        ImportDeclaration imp = new ImportDeclaration(className);
        Map<String, String> metaData = getMetadataFromImportDeclaration(imp);
        ConfigItem<ImportDeclaration> cnf = new ConfigItem<ImportDeclaration>(metaData, imp);
        try {
            if (persistenceService.load(metaData).isEmpty()) {
                persistenceService.persist(cnf);
            }
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadRulebase();
    }

    @Override
    public void removeImport(String className) throws RuleBaseException {
        try {
            ImportDeclaration imp = new ImportDeclaration(className);
            Map<String, String> metaData = getMetadataFromImportDeclaration(imp);
            persistenceService.remove(metaData);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadRulebase();
    }

    @Override
    public Collection<String> listImports() {
        ImportDeclaration imp = new ImportDeclaration();
        Map<String, String> metaData = getMetadataFromImportDeclaration(imp);
        List<ConfigItem<ImportDeclaration>> queryResult;
        try {
            queryResult = persistenceService.load(metaData);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        Collection<String> result = new HashSet<String>();
        for (ConfigItem<ImportDeclaration> i : queryResult) {
            result.add(i.getContent().getClassName());
        }
        return result;
    }

    @Override
    public void addGlobal(String className, String name) throws RuleBaseException {
        GlobalDeclaration globalDeclaration = new GlobalDeclaration(name);
        Map<String, String> metaData = getMetadataFromGlobalDeclaration(globalDeclaration);
        List<ConfigItem<GlobalDeclaration>> queryResult;
        try {
            queryResult = persistenceService.load(metaData);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        if (!queryResult.isEmpty()) {
            throw new RuleBaseException(String.format("Global with name \"%s\" already exists", name));
        }
        globalDeclaration.setClassName(className);
        ConfigItem<GlobalDeclaration> cnf = new ConfigItem<GlobalDeclaration>(metaData, globalDeclaration);
        try {
            persistenceService.persist(cnf);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadRulebase();
    }

    @Override
    public void removeGlobal(String name) throws RuleBaseException {
        GlobalDeclaration globalDeclaration = new GlobalDeclaration(name);
        Map<String, String> metaData = getMetadataFromGlobalDeclaration(globalDeclaration);
        try {
            persistenceService.remove(metaData);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        builder.reloadRulebase();
    }

    @Override
    public Map<String, String> listGlobals() {
        GlobalDeclaration globalDeclaration = new GlobalDeclaration();
        Map<String, String> metaData = getMetadataFromGlobalDeclaration(globalDeclaration);
        List<ConfigItem<GlobalDeclaration>> queryResult;
        try {
            queryResult = persistenceService.load(metaData);
        } catch (PersistenceException e) {
            throw new RuleBaseException(e);
        }
        Map<String, String> globals = new HashMap<String, String>();
        for (ConfigItem<GlobalDeclaration> g : queryResult) {
            globals.put(g.getContent().getVariableName(), g.getContent().getClassName());
        }
        return globals;
    }

    public void setPersistenceService(ConfigPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    private Map<String, String> getMetadataFromRuleBaseElement(RuleBaseElement element) {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put(META_RULE, META_RULE);
        if (element.getName() != null) {
            ret.put(META_RULE_NAME, element.getName());
        }
        if (element.getPackageName() != null) {
            ret.put(META_RULE_PACKAGE, element.getPackageName());
        }
        if (element.getType() != null) {
            ret.put(META_RULE_TYPE, element.getType().toString());
        }
        return ret;
    }

    private Map<String, String> getMetadataFromImportDeclaration(ImportDeclaration element) {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put(META_IMPORT, META_IMPORT);
        if (element.getClassName() != null) {
            ret.put(META_IMPORT_CLASS, element.getClassName());
        }
        return ret;

    }

    private Map<String, String> getMetadataFromGlobalDeclaration(GlobalDeclaration element) {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put(META_GLOBAL, META_GLOBAL);
        if (element.getVariableName() != null) {
            ret.put(META_GLOBAL_VARIABLE, element.getVariableName());
        }
        return ret;

    }
}
