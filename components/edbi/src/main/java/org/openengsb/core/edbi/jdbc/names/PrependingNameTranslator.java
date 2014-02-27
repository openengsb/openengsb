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

import org.openengsb.core.edbi.api.NameTranslator;

/**
 * Decorator that prepends a defined string to the result of another {@link org.openengsb.core.edbi.api.NameTranslator}.
 */
public class PrependingNameTranslator<T> implements NameTranslator<T> {

    /**
     * The wrapped translator
     */
    private NameTranslator<T> translator;

    /**
     * The prefix
     */
    private String prefix;

    public PrependingNameTranslator(NameTranslator<T> translator) {
        this(translator, "");
    }

    public PrependingNameTranslator(NameTranslator<T> translator, String prefix) {
        this.translator = translator;
        this.prefix = prefix;
    }

    @Override
    public String translate(T value) {
        return getPrefix() + translator.translate(value);
    }

    /**
     * Returns the prefix that will be added to the translation result. Can be overridden by subclasses.
     * 
     * @return the prefix to add
     */
    protected String getPrefix() {
        return prefix;
    }
}
