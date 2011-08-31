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

import java.io.File;
import java.util.Map;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.openengsb.core.workflow.model.RuleBaseElement;

public class RuleBaseFileNameFilter implements IOFileFilter {

    private String type;
    private String name;
    private String pack;

    public RuleBaseFileNameFilter(Map<String, String> metaData) {
        type = metaData.get(RuleBaseElement.META_RULE_TYPE);
        name = metaData.get(RuleBaseElement.META_RULE_NAME);
        pack = metaData.get(RuleBaseElement.META_RULE_PACKAGE);
    }

    @Override
    public boolean accept(File file) {
        String filename = file.getName();
        String[] parts = filename.split(RuleBaseElementPersistenceBackendService.SEPARATOR);

        if (parts.length != 3) {
            return false;
        }

        if (!(type == null || parts[0].equals(type))) {
            return false;
        }

        if (!(name == null || parts[1].equals(name))) {
            return false;
        }

        if (!(pack == null || parts[2].equals(pack))) {
            return false;
        }

        return true;

    }

    @Override
    public boolean accept(File dir, String name) {
        throw new UnsupportedOperationException("Method is not needed!");
    }
}
