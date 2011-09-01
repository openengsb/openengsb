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

package org.openengsb.ui.common.model;

import java.util.Hashtable;

import org.openengsb.core.api.OAuthData;

/**
 * The problem with oauth is that the callback come in on a different place than the original call. Therfore we have to
 * build a "bridge" to handle such cases. This class is static and used to transfer such objects between various threads
 * and objects.
 */
public final class OAuthPageFactory {

    private OAuthPageFactory() {
    }

    static Hashtable<String, OAuthData> oAuthObjects = new Hashtable<String, OAuthData>();

    public static OAuthData getOAuthObject(String sessionID) {
        OAuthData tmp = oAuthObjects.get(sessionID);
        return tmp;
    }

    public static void removeOAuthObject(String sessionID) {
        oAuthObjects.remove(sessionID);
    }

    public static void putOAuthObject(String sessionID, OAuthData object) {
        oAuthObjects.put(sessionID, object);
    }
}
