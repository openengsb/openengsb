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

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openengsb.connector.virtual.filewatcher.FileSerializer;

public class CSVFileParserTest extends AbstractParserTest {

    @Override
    protected FileSerializer<TestModel> createSerializer() {
        return new CSVParser<TestModel>(TestModel.class);
    }

    @Test
    public void testParseCSVShouldCreateObject() throws Exception {
        FileUtils.write(testfile, ""
                + "42,\"foo\", 7\n"
                + "21,\"bar\", 9\n");
        List<TestModel> testModels = parser.readFile(testfile);
        assertThat(testModels.size(), is(2));
        TestModel model = testModels.get(0);
        assertThat(model.getA(), is(42));
        assertThat(model.getB(), is("foo"));
        assertThat(model.getC(), is(7L));
        model = testModels.get(1);
        assertThat(model.getA(), is(21));
        assertThat(model.getB(), is("bar"));
        assertThat(model.getC(), is(9L));
    }
}
