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
package org.openengsb.core.workflow;

import java.util.Collection;

import org.drools.RuleBase;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;
import org.openengsb.core.workflow.source.RuleBaseException;

public interface RuleManager {

    RuleBase getRulebase() throws RuleBaseException;

    void add(RuleBaseElementId name, String code) throws RuleBaseException;

    String get(RuleBaseElementId name) throws RuleBaseException;

    void update(RuleBaseElementId name, String newCode) throws RuleBaseException;

    void delete(RuleBaseElementId name) throws RuleBaseException;

    Collection<RuleBaseElementId> list(RuleBaseElementType type) throws RuleBaseException;

    Collection<RuleBaseElementId> list(RuleBaseElementType type, String packageName) throws RuleBaseException;

}