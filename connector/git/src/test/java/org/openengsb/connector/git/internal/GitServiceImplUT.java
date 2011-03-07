/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.connector.git.internal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.junit.Test;

/**
 * This class contains unit-tests which may require a special system setup and
 * should not be run automatically therefore.
 */
public class GitServiceImplUT extends AbstractGitServiceImpl {

    @Test
    public void pollWithEmptyWorkspace_shouldCloneSSHRemoteRepository() throws IOException {
        service.setRemoteLocation("git@github.com:Mercynary/myTestRepo.git");
        service.update();
        localRepository = service.getRepository();
        AnyObjectId id = localRepository.resolve(Constants.HEAD);
        assertThat(id, notNullValue());
        assertThat(id.name(), is("2f610959a14c8f26549bee563ad4da8c65e1ee8b"));

    }
}
