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

package org.openengsb.core.edbi.jdbc.names;

import org.apache.commons.codec.digest.DigestUtils;
import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexNameTranslator;

/**
 * Implementation of a IndexNameTranslator that creates a unique SQL name usable as table name.
 */
public class SQLIndexNameTranslator implements IndexNameTranslator {

    protected static final int DEFAULT_MAXLEN = 10;

    @Override
    public String translate(Index<?> index) {
        if (index == null) {
            throw new IllegalArgumentException("Class to translate is null");
        }

        return DigestUtils.shaHex(index.getName()).substring(0, DEFAULT_MAXLEN).toUpperCase();
    }
}
