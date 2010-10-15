/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.persistence.internal;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.openengsb.core.persistence.PersistenceService;
import org.openengsb.core.persistence.PersistenceServiceTest;

public class NeodatisPersistenceServiceTest extends PersistenceServiceTest {

    private NeodatisPersistenceService persistence;

    @Override
    protected PersistenceService createPersitenceService() throws Exception {
        persistence = new NeodatisPersistenceService("target/db.data", getClass().getClassLoader());
        return persistence;
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.forceDelete(new File("target/db.data"));
    }

}
