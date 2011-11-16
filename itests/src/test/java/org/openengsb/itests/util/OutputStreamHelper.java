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

package org.openengsb.itests.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a helper class for the console integration tests.
 * Its purpose is to gather the result of the executed command in an ArrayList.
 */
public class OutputStreamHelper extends OutputStream {

    private List<String> result = new ArrayList<String>();

    @Override
    public void write(int i) throws IOException {
        // ignore
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        super.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws IOException {
        String substring = new String(bytes).substring(i, i1);
        // filter out some stuff like empty strings or new line stuff
        if (substring != null && !"".equals(substring) && !"\n".equals(substring)) {
            result.add(substring);
        }
        super.write(bytes, i, i1);
    }

    public List<String> getResult() {
        return result;
    }
}
