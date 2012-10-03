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

package org.openengsb.core.workflow.drools.internal;

import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.definition.KnowledgePackage;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.openengsb.core.workflow.api.RuleBaseException;
import org.openengsb.core.workflow.api.RuleManager;
import org.openengsb.core.workflow.api.model.RuleBaseElementId;
import org.openengsb.core.workflow.api.model.RuleBaseElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class RulebaseBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RulebaseBuilder.class);

    private static final String RULE_TEMPLATE = "rule \"%s\"\n"
            + "%s"
            + "end\n";

    private final KnowledgeBase base;
    private final RuleManager manager;

    private String declarations;
    private Map<String, StringBuffer> packageStrings = new HashMap<String, StringBuffer>();

    public RulebaseBuilder(KnowledgeBase base, RuleManager manager) {
        this.base = base;
        this.manager = manager;
    }

    /**
     * reloads the rulebase but keeps references intact
     *
     * @throws RuleBaseException if the rulebase contains errors
     */
    public synchronized void reloadRulebase() throws RuleBaseException {
        long start = System.currentTimeMillis();
        reloadDeclarations();
        packageStrings.clear();

        for (RuleBaseElementId id : manager.listAll(RuleBaseElementType.Function)) {
            String packageName = id.getPackageName();
            StringBuffer packageString = getPackageString(packageName);
            String code = manager.get(id);
            packageString.append(code);
        }
        for (RuleBaseElementId id : manager.listAll(RuleBaseElementType.Rule)) {
            String packageName = id.getPackageName();
            StringBuffer packageString = getPackageString(packageName);
            String code = manager.get(id);
            String formattedRule = String.format(RULE_TEMPLATE, id.getName(), code);
            packageString.append(formattedRule);
        }
        for (RuleBaseElementId id : manager.listAll(RuleBaseElementType.Process)) {
            getPackageString(id.getPackageName());
        }
        Collection<KnowledgePackage> compiledPackages = new HashSet<KnowledgePackage>();
        if (packageStrings.isEmpty()) {
            Set<String> emptySet = Collections.emptySet();
            compiledPackages.addAll(compileDrlString("package dummy;\n" + declarations, emptySet));
        } else {
            for (Map.Entry<String, StringBuffer> entry : packageStrings.entrySet()) {
                String packageName = entry.getKey();
                StringBuffer drlCode = entry.getValue();
                Collection<String> flows = queryFlows(packageName);
                Collection<KnowledgePackage> compiledDrlPackage = compileDrlString(drlCode.toString(), flows);
                compiledPackages.addAll(compiledDrlPackage);
            }
        }
        lockRuleBase();
        clearRulebase();
        base.addKnowledgePackages(compiledPackages);
        unlockRuleBase();
        LOGGER.info("Reloading the rulebase took {}ms", System.currentTimeMillis() - start);
    }

    private Collection<String> queryFlows(final String packageName) {
        Collection<RuleBaseElementId> list = manager.listAll(RuleBaseElementType.Process);
        Collection<RuleBaseElementId> filtered = Collections2.filter(list, new Predicate<RuleBaseElementId>() {
            @Override
            public boolean apply(RuleBaseElementId input) {
                return input.getPackageName().equals(packageName);
            }
        });
        return Collections2.transform(filtered, new Function<RuleBaseElementId, String>() {
            @Override
            public String apply(RuleBaseElementId input) {
                return manager.get(input);
            }
        });
    }

    public synchronized void reloadPackage(String packageName) throws RuleBaseException {
        long start = System.currentTimeMillis();
        reloadDeclarations();
        packageStrings.clear();
        StringBuffer packageString = initNewPackageString(packageName);

        for (RuleBaseElementId id : manager.list(RuleBaseElementType.Function, packageName)) {
            String code = manager.get(id);
            packageString.append(code);
        }
        for (RuleBaseElementId id : manager.list(RuleBaseElementType.Rule, packageName)) {
            String code = manager.get(id);
            String formattedRule = String.format(RULE_TEMPLATE, id.getName(), code);
            packageString.append(formattedRule);
        }
        Collection<String> flows = queryFlows(packageName);
        Collection<KnowledgePackage> compiledPackage = compileDrlString(packageString.toString(), flows);
        lockRuleBase();
        if (base.getKnowledgePackage(packageName) != null) {
            base.removeKnowledgePackage(packageName);
        }
        base.addKnowledgePackages(compiledPackage);
        unlockRuleBase();
        LOGGER.info("Reloading only package {} took {}ms", packageName, System.currentTimeMillis() - start);
    }

    private void unlockRuleBase() {
        ((KnowledgeBaseImpl) base).ruleBase.unlock();
    }

    private void lockRuleBase() {
        ((KnowledgeBaseImpl) base).ruleBase.lock();
    }

    private void clearRulebase() {
        for (KnowledgePackage p : base.getKnowledgePackages()) {
            base.removeKnowledgePackage(p.getName());
        }
    }

    private StringBuffer getPackageString(String packageName) {
        StringBuffer result = packageStrings.get(packageName);
        if (result == null) {
            result = initNewPackageString(packageName);
            packageStrings.put(packageName, result);
        }
        return result;
    }

    private StringBuffer initNewPackageString(String packageName) {
        StringBuffer result = new StringBuffer();
        result.append(String.format("package %s;", packageName));
        if (declarations != null) {
            result.append(declarations);
        }
        return result;
    }

    private void reloadDeclarations() {
        StringBuffer prelude = new StringBuffer();
        for (String imp : manager.listImports()) {
            prelude.append(String.format("import %s;\n", imp));
        }
        for (Map.Entry<String, String> global : manager.listGlobals().entrySet()) {
            prelude.append(String.format("global %s %s;\n", global.getValue(), global.getKey()));
        }
        declarations = prelude.toString();
    }

    private Collection<KnowledgePackage> compileDrlString(String content, Collection<String> flows)
        throws RuleBaseException {
        KnowledgeBuilder builder = getConfiguredBuilder();
        builder.add(ResourceFactory.newReaderResource(new StringReader(content)), ResourceType.DRL);
        if (flows != null) {
            for (String drf : flows) {
                Resource resource = ResourceFactory.newReaderResource(new StringReader(drf));
                builder.add(resource, ResourceType.DRF);
            }
        }
        if (builder.hasErrors()) {
            throw new RuleBaseException(builder.getErrors().toString());
        }
        return builder.getKnowledgePackages();
    }

    private KnowledgeBuilder getConfiguredBuilder() {
        Properties properties = new Properties();
        properties.setProperty("drools.dialect.java.compiler", "JANINO");
        PackageBuilderConfiguration conf = new PackageBuilderConfiguration(properties);
        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder(conf);
        return builder;
    }
}
