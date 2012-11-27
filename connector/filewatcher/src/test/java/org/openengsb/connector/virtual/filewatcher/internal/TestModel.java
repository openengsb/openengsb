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

import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;

@Model
public class TestModel {

    private int a;
    @OpenEngSBModelId
    private String b;
    private long c;

    public TestModel() {
    }

    public TestModel(int a, String b, long c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public long getC() {
        return c;
    }

    public void setC(long c) {
        this.c = c;
    }

    @Override
    public String toString() {
        return "TestModel{"
                + "a=" + a
                + ", b='" + b + '\''
                + ", c=" + c
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TestModel)) {
            return false;
        }

        TestModel testModel = (TestModel) o;

        if (a != testModel.a) {
            return false;
        }
        if (c != testModel.c) {
            return false;
        }
        if (b != null ? !b.equals(testModel.b) : testModel.b != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = a;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        result = 31 * result + (int) (c ^ (c >>> 32));
        return result;
    }
}
