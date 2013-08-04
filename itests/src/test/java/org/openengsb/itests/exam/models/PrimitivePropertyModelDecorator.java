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

package org.openengsb.itests.exam.models;

import org.apache.commons.beanutils.MethodUtils;

public class PrimitivePropertyModelDecorator {
    private Object model;

    public PrimitivePropertyModelDecorator(Object model) {
        this.model = model;
    }

    public Object getModel() {
        return model;
    }

    public String getId() throws Exception {
        return (String) MethodUtils.invokeMethod(model, "getId", null);
    }

    public void setId(String id) throws Exception {
        MethodUtils.invokeMethod(model, "setId", id);
    }

    public boolean getBooleanByGet() throws Exception {
        return (boolean) MethodUtils.invokeMethod(model, "getBooleanByGet", null);
    }

    public void setBooleanByGet(boolean booleanByGet) throws Exception {
        MethodUtils.invokeMethod(model, "setBooleanByGet", booleanByGet);
    }

    public boolean isBooleanByIs() throws Exception {
        return (boolean) MethodUtils.invokeMethod(model, "isBooleanByIs", null);
    }

    public void setBooleanByIs(boolean booleanByIs) throws Exception {
        MethodUtils.invokeMethod(model, "setBooleanByIs", booleanByIs);
    }

    public short getPrimitiveShort() throws Exception {
        return (short) MethodUtils.invokeMethod(model, "getPrimitiveShort", null);
    }

    public void setPrimitiveShort(short primitiveShort) throws Exception {
        MethodUtils.invokeMethod(model, "setPrimitiveShort", primitiveShort);
    }

    public int getPrimitiveInt() throws Exception {
        return (int) MethodUtils.invokeMethod(model, "getPrimitiveInt", null);
    }

    public void setPrimitiveInt(int primitiveInt) throws Exception {
        MethodUtils.invokeMethod(model, "setPrimitiveInt", primitiveInt);
    }

    public long getPrimitiveLong() throws Exception {
        return (long) MethodUtils.invokeMethod(model, "getPrimitiveLong", null);
    }

    public void setPrimitiveLong(long primitiveLong) throws Exception {
        MethodUtils.invokeMethod(model, "setPrimitiveLong", primitiveLong);
    }

    public double getPrimitiveDouble() throws Exception {
        return (double) MethodUtils.invokeMethod(model, "getPrimitiveDouble", null);
    }

    public void setPrimitiveDouble(double primitiveDouble) throws Exception {
        MethodUtils.invokeMethod(model, "setPrimitiveDouble", primitiveDouble);
    }

    public float getPrimitiveFloat() throws Exception {
        return (float) MethodUtils.invokeMethod(model, "getPrimitiveFloat", null);
    }

    public void setPrimitiveFloat(float primitiveFloat) throws Exception {
        MethodUtils.invokeMethod(model, "setPrimitiveFloat", primitiveFloat);
    }

    public char getPrimitiveChar() throws Exception {
        return (char) MethodUtils.invokeMethod(model, "getPrimitiveChar", null);
    }

    public void setPrimitiveChar(char primitiveChar) throws Exception {
        MethodUtils.invokeMethod(model, "setPrimitiveChar", primitiveChar);
    }

}
