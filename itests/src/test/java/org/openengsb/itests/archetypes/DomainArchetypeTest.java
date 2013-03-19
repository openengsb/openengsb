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

package org.openengsb.itests.archetypes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DomainArchetypeTest extends AbstractArchetypeTest {

    @Override
    protected void addArchetypeData(Properties properties) {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream("archetype.domain.properties");
        try {
            prop.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Object key : prop.keySet()) {
            properties.setProperty((String) key, (String) prop.get(key));
        }
    }

    @Override
    protected void applyProjectModifications() throws Exception {
        // TODO Auto-generated method stub

    }

}
