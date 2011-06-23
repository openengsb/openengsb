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

package org.openengsb.ui.admin.tree;

import java.io.Serializable;

import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.context.ContextHolder;

@SuppressWarnings("serial")
public class ModelBean implements Serializable {

    private final ContextCurrentService contextService;
    private final String key;
    private final boolean isLeaf;

    public ModelBean(ContextCurrentService contextService, String key, boolean isLeaf) {
        this.contextService = contextService;
        this.key = key;
        this.isLeaf = isLeaf;
    }

    public String getKey() {
        return key;
    }

    public String getNiceKey() {
        String[] path = key.split("/");
        if (path.length - 1 >= 0) {
            return path[path.length - 1];
        } else {
            return ContextHolder.get().getCurrentContextId();
        }
    }

    public String getValue() {
        return contextService.getValue(key);
    }

    public void setValue(String value) {
        if (isLeaf) {
            String pushValue = "";
            if (value != null) {
                pushValue = value;
            }
            contextService.putValue(key, pushValue);
        }
    }

    public boolean isLeaf() {
        return isLeaf;
    }
}
