/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */

package org.openengsb.core.methodcalltransformation;

public class MethodCall {
    private final String methodName;
    private final Object[] args;
    private final Class<?>[] types;

    public MethodCall(String methodName, Object[] args, Class<?>[] types) {
        this.methodName = methodName;
        this.args = args;
        this.types = types;
    }

    public Class<?>[] getTypes() {
        return types;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }
}