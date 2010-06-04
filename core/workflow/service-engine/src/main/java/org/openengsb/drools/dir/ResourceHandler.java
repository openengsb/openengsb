/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.drools.dir;

import org.openengsb.drools.RuleBaseException;
import org.openengsb.drools.RuleBaseSource;

public abstract class ResourceHandler<T extends RuleBaseSource> {

    protected T source;

    public ResourceHandler(T source) {
        this.source = source;
    }

    public abstract void create(String name, String code) throws RuleBaseException;

    public abstract String get(String name) throws RuleBaseException;

    public void update(String name, String code) throws RuleBaseException {
        delete(name);
        create(name, code);
    }

    public abstract void delete(String name) throws RuleBaseException;
}
