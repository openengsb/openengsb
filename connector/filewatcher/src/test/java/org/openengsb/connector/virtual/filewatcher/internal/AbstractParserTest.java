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
package org.openengsb.connector.virtual.filewatcher.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.connector.virtual.filewatcher.FileSerializer;

public abstract class AbstractParserTest {


    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    protected FileSerializer<TestModel> parser;
    protected File testfile;

    @Before
    public void setupAbstractParserTest() throws Exception {
        testfile = temporaryFolder.newFile("testfile.txt");
        parser = createSerializer();
    }

    protected abstract FileSerializer<TestModel> createSerializer();

    @Test
    public void testWriteDataAndParseShouldBeTheSame() throws Exception {
        List<TestModel> models = Arrays.asList(
                new TestModel(42, "foo", 7L),
                new TestModel(21, "bar", 9L)
        );
        parser.writeFile(testfile, models);
        List<TestModel> parsedModels = parser.readFile(testfile);
        assertThat(parsedModels, is(models));
    }

}
