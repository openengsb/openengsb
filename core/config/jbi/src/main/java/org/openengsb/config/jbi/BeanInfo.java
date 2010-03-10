/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.config.jbi;

import java.util.HashMap;
import java.util.Map;

import org.openengsb.config.jbi.types.BeanType;

public class BeanInfo {
    private final BeanType beanType;
    private final Map<String, String> map;

    public BeanInfo(BeanType beanType) {
        this(beanType, new HashMap<String, String>());
    }

    public BeanInfo(BeanType beanType, Map<String, String> map) {
        this.beanType = beanType;
        this.map = map;
    }

    public BeanType getBeanType() {
        return beanType;
    }

    public Map<String, String> getMap() {
        return map;
    }
}
