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

package org.openengsb.core.edb.jpa.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.openengsb.core.edb.api.EDBObject;

public class Utils {

    private static Random rand;

    public Utils() {
        long seed = System.currentTimeMillis();
        rand = new Random(seed);
    }

    private static final String[] RANDOMKEYS = new String[]{
        "Product", "Handler", "RandomKey", "UserID", "Code", "Auto"
    };

    private static final String[] RANDOMCOMMITTERS = new String[]{
        "Bernard", "Johnny", "Jack", "Christian", "Latehost", "Panda"
    };

    private static final String[] RANDOMROLES = new String[]{
        "Modeller", "Designer", "Programmer", "Annoying Person", "Bossy Bastard"
    };

    public EDBObject createRandomTestObject(String oid) {
        Map<String, Object> testData = new HashMap<String, Object>();
        int max = 5;

        for (int i = 0; i < max; ++i) {
            String key = RANDOMKEYS[rand.nextInt(RANDOMKEYS.length)] + Integer.toString(i);
            String value = "key value " + Integer.toString(rand.nextInt(100));
            testData.put(key, value);
        }
        return new EDBObject(oid, testData);
    }

    public String getRandomCommitter() {
        return RANDOMCOMMITTERS[rand.nextInt(RANDOMCOMMITTERS.length)];
    }

    public String getRandomRole() {
        return RANDOMROLES[rand.nextInt(RANDOMROLES.length)];
    }

}
