/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.drools.source;

import java.util.Collection;

import org.drools.RuleBase;
import org.drools.rule.Package;
import org.openengsb.drools.RuleBaseException;

public abstract class RuleBaseSource {

    public static final String DEFAULT_RULE_PACKAGE = "org.openengsb";

    protected ResourceHandler<?> getRessourceHandler(RuleBaseElement element) {
        throw new UnsupportedOperationException("not implemented for type " + getClass());
    }

    public abstract RuleBase getRulebase() throws RuleBaseException;

    /* CRUD */
    public void add(RuleBaseElement type, String name, String code) throws RuleBaseException {
        this.getRessourceHandler(type).create(name, code);
    }

    public String get(RuleBaseElement type, String name) throws RuleBaseException {
        return this.getRessourceHandler(type).get(name);
    }

    public void update(RuleBaseElement type, String name, String newCode) throws RuleBaseException {
        this.getRessourceHandler(type).update(name, newCode);
    }

    public void delete(RuleBaseElement type, String name) throws RuleBaseException {
        this.getRessourceHandler(type).delete(name);
    }

    public Collection<String> list(RuleBaseElement type) throws RuleBaseException {
        return this.getRessourceHandler(type).list();
    }

    public Package getPackage() throws RuleBaseException {
        return getRulebase().getPackage(DEFAULT_RULE_PACKAGE);
    }
}
