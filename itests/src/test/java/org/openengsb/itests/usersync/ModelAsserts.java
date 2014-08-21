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
package org.openengsb.itests.usersync;

import static org.junit.Assert.assertEquals;

import org.openengsb.domain.userprojects.model.Attribute;
import org.openengsb.domain.userprojects.model.User;

/**
 * Assertions for User models.
 */
public final class ModelAsserts {

    private ModelAsserts() {
        // static utility class
    }

    public static void assertEqualUser(User user1, User user2) {
        assertEquals(user1.getUsername(), user2.getUsername());
        assertEquals(user1.getAttributes().size(), user2.getAttributes().size());

        for (int i = 0; i < user1.getAttributes().size(); i++) {
            Attribute attr1 = user1.getAttributes().get(i);
            Attribute attr2 = user2.getAttributes().get(i);

            assertEquals(attr1.getAttributeName(), attr2.getAttributeName());
            assertEquals(attr1.getUuid(), attr2.getUuid());
            assertEquals(attr1.getValues().size(), attr2.getValues().size());

            for (int n = 0; n < attr1.getValues().size(); n++) {
                assertEquals(attr1.getValues().get(n), attr2.getValues().get(n));
            }
        }
    }

}
