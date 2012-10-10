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

package org.openengsb.core.ekb.transformation.wonderland.internal.operation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.openengsb.core.ekb.api.transformation.TransformationOperationException;
import org.slf4j.Logger;

/**
 * The OperationUtils class contains some common functions for the standard transformation operation set of the
 * OpenEngSB project.
 */
public final class OperationUtils {

    private OperationUtils() {
    }

    /**
     * Generates a matcher for the given valueString with the given regular expression.
     */
    public static Matcher generateMatcher(String regex, String valueString, Logger logger)
        throws TransformationOperationException {
        if (regex == null) {
            throw new TransformationOperationException("No regex defined. The step will be skipped.");
        }
        try {
            Pattern pattern = Pattern.compile(regex);
            return pattern.matcher(valueString);
        } catch (PatternSyntaxException e) {
            String message =
                String.format("Given regex string %s can't be compiled. The step will be skipped.", regex);
            logger.warn(message);
            throw new TransformationOperationException(message);
        }
    }

    /**
     * Parses a string to an integer object. AbortOnError defines if the function should throw an exception if an error
     * occurs during the parsing. If it doesn't abort, the given default value is given back as result on error.
     */
    public static Integer parseIntString(String string, boolean abortOnError, int def, Logger logger)
        throws TransformationOperationException {
        Integer integer = def;
        if (string == null) {
            logger.debug("Given string is empty so the default value is taken.");
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

            logger.warn(builder.toString());
            if (abortOnError) {
                throw new TransformationOperationException(builder.toString());
            }
        }
        return integer;
    }

}
