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

package org.openengsb.core.common.security;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.security.Key;

import org.junit.Test;

public class MessageCipherTest {

    @Test
    public void testEncryptStringDecrypt() throws Exception {
        Key key = new SecretKeyUtil("Blowfish").generateKey(256);
        CipherUtil cipher = new SecretKeyCipherUtil("Blowfish");
        StringMessageCipher messageCipher = new StringMessageCipher(cipher, key);
        String testMessage = "The answer to life, the universe and everything";
        String encrypt = messageCipher.encrypt(testMessage);
        System.out.println(encrypt);

        String decrypt = messageCipher.decrypt(encrypt);
        assertThat(decrypt, is(testMessage));
    }

}
