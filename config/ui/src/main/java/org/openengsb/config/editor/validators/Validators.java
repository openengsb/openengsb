/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.config.editor.validators;

import java.util.ArrayList;

import org.apache.wicket.validation.IValidator;
import org.openengsb.config.jbi.types.AbstractType;
import org.openengsb.config.jbi.types.IntType;

public class Validators {
    @SuppressWarnings("unchecked")
    public static IValidator<String>[] buildValidators(AbstractType type) {
        ArrayList<IValidator<String>> v = new ArrayList<IValidator<String>>();
        if (type.getClass().equals(IntType.class)) {
            v.addAll(IntTypeValidators.buildValidators((IntType) type));
        }
        return v.toArray(new IValidator[v.size()]);
    }
}
