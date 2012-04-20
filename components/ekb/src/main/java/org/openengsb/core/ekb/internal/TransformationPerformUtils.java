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

package org.openengsb.core.ekb.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils class to take away some simple functions from the TransformationPerformer
 */
public final class TransformationPerformUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationPerformUtils.class);

    private TransformationPerformUtils() {
    }

    /**
     * Generates a matcher for the given valueString with the given regular expression.
     */
    public static Matcher generateMatcher(String regex, String valueString) throws TransformationStepException {
        if (regex == null) {
            throw new TransformationStepException("No regex defined. The step will be skipped.");
        }
        try {
            Pattern pattern = Pattern.compile(regex);
            return pattern.matcher(valueString);
        } catch (PatternSyntaxException e) {
            String message =
                String.format("Given regex string %s can't be compiled. The step will be skipped.", regex);
            LOGGER.warn(message);
            throw new TransformationStepException(message);
        }
    }

    /**
     * Parses a string to an integer object. AbortOnError defines if the function should throw an exception if an error
     * occurs during the parsing. If it doesn't abort, the given default value is given back as result on error.
     */
    public static Integer parseIntString(String string, boolean abortOnError, int def)
        throws TransformationStepException {
        Integer integer = def;
        if (string == null) {
            LOGGER.debug("Given string is empty so the default value is taken.");
        }
        try {
            integer = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            StringBuilder builder = new StringBuilder();
            builder.append("The string ").append(string).append(" is not a number. ");
            if (abortOnError) {
                builder.append("The step will be skipped.");
            } else {
                builder.append(def).append(" will be taken instead.");
            }

            LOGGER.warn(builder.toString());
            if (abortOnError) {
                throw new TransformationStepException(builder.toString());
            }
        }
        return integer;
    }
    
    /**
     * Returns true if the given fieldname points to a temporary field. Returns false if not.
     */
    public static boolean isTemporaryField(String fieldname) {
        return fieldname.startsWith("temp.");
    }

    /**
     * Returns the name of the getter method of a field.
     */
    public static String getGetterName(String fieldname) {
        return "get" + Character.toUpperCase(fieldname.charAt(0)) + fieldname.substring(1);
    }

    /**
     * Returns the name of the setter method of a field.
     */
    public static String getSetterName(String fieldname) {
        return "set" + Character.toUpperCase(fieldname.charAt(0)) + fieldname.substring(1);
    }
}
