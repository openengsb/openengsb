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

package org.openengsb.connector.authentication.jira.internal;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.domain.authentication.AuthenticationException;

public class AuthenticationFileServiceImpl extends AbstractOpenEngSBConnectorService implements AuthenticationDomain {

    public AuthenticationFileServiceImpl() {
    }

    @Override
    public AliveState getAliveState() {
        if (Configuration.get().getUsernamePasswordFile().exists()) {
            return AliveState.ONLINE;
        }
        return AliveState.OFFLINE;
    }

    @Override
    public Authentication authenticate(String username, Credentials credentials) throws AuthenticationException {
        String givenPassword = ((Password) credentials).getValue();
        String actualPassword = null;
        try {
            actualPassword = findPasswordByUsername(username);
        } catch (IOException e) {
            throw new AuthenticationException(e);
        }
        if (actualPassword == null || !actualPassword.equals(givenPassword)) {
            throw new AuthenticationException();
        }
        Authentication authentication = new Authentication(username);
        return authentication;
    }

    private String findPasswordByUsername(String username) throws IOException {
        List<String> lines = FileUtils.readLines(Configuration.get().getUsernamePasswordFile());
        for (String line : lines) {
            String[] usernamePassword = StringUtils.split(line, Configuration.get().getAssociationSeparator());
            if (usernamePassword[0].equals(username)) {
                return usernamePassword[1];
            }
        }
        return null;
    }

    @Override
    public boolean supports(Credentials credentials) {
        return credentials instanceof Password;
    }

}
