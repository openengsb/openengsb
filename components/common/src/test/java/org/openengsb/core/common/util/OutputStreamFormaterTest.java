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

package org.openengsb.core.common.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.PrintStream;

import org.junit.Test;

public class OutputStreamFormaterTest {

    @Test
    public void testSpaces_shouldInsertSpaces() throws Exception {
        assertThat(OutputStreamFormater.spaces(20).length(), is(20));
    }

    @Test
    public void testFormatValues_shouldFormatString() throws Exception {
        String string = "  \u001B[1mOpenEngSB Version        \u001B[m   3.0.0-SNAPSHOT";
        assertThat(OutputStreamFormater.formatValues("OpenEngSB Version", "3.0.0-SNAPSHOT"), equalTo(string));
    }

    @Test
    public void testPrintValue_shouldPrintValue() throws Exception {
        PrintStream streamMock = mock(PrintStream.class);
        OutputStreamFormater.setOutputStream(streamMock);
        OutputStreamFormater.printValue("OpenEngSB");
        verify(streamMock, times(1)).println("OpenEngSB");
    }

    @Test
    public void testPrintValues_shouldPrintValues() throws Exception {
        String string = "  \u001B[1mOpenEngSB Version        \u001B[m   3.0.0-SNAPSHOT";
        PrintStream streamMock = mock(PrintStream.class);
        OutputStreamFormater.setOutputStream(streamMock);
        OutputStreamFormater.printValue("OpenEngSB Version", "3.0.0-SNAPSHOT");
        verify(streamMock, times(1)).println(string);
    }

    @Test
    public void testPrintWithPrefix_shouldPrintStringWithPrefix() throws Exception {
        String s = OutputStreamFormater.formatValues(9, "id", "status");
        assertThat(s, equalTo("         \u001B[1mid                       \u001B[m   status"));
    }
}
