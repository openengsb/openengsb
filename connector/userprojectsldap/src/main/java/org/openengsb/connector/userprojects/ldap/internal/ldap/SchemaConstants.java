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

package org.openengsb.connector.userprojects.ldap.internal.ldap;

/**
 * This class contains constants representing various data in the DIT.
 * */
public final class SchemaConstants {

    public static final String BASE_DN = "dc=openengsb,dc=org";
    public static final String ORGANIZATIONAL_UNIT_OC = "organizationalUnit";

    public static final String CN_ATTRIBUTE = "cn";
    public static final String JAVA_CLASS_NAME_ATTRIBUTE = "javaClassName";
    public static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";
    public static final String OU_ATTRIBUTE = "ou";

    public static final String JAVA_CLASS_INSTANCE_OC = "org-openengsb-javaClassInstance";
    public static final String DESCRIPTIVE_OBJECT_OC = "org-openengsb-descriptiveObject";
    public static final String NAMED_OBJECT_OC = "org-openengsb-namedObject";

    public static final String STRING_ATTRIBUTE = "org-openengsb-string";
    public static final String EMPTY_FLAG_ATTRIBUTE = "org-openengsb-emptyFlag";

    private SchemaConstants() {
    }

}
