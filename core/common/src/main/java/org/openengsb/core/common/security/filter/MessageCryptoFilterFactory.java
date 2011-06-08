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

package org.openengsb.core.common.security.filter;

import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterChainElementFactory;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.common.security.PrivateKeySource;

public class MessageCryptoFilterFactory implements FilterChainElementFactory {

    private PrivateKeySource privateKeySource;
    private String secretKeyAlgorithm;

    public MessageCryptoFilterFactory() {
    }

    public MessageCryptoFilterFactory(PrivateKeySource privateKeySource, String secretKeyAlgorithm) {
        this.privateKeySource = privateKeySource;
        this.secretKeyAlgorithm = secretKeyAlgorithm;
    }

    @Override
    public FilterChainElement newInstance() throws FilterConfigurationException {
        return new MessageCryptoFilter(privateKeySource, secretKeyAlgorithm);
    }

    public void setPrivateKeySource(PrivateKeySource privateKeySource) {
        this.privateKeySource = privateKeySource;
    }

    public void setSecretKeyAlgorithm(String secretKeyAlgorithm) {
        this.secretKeyAlgorithm = secretKeyAlgorithm;
    }

}
