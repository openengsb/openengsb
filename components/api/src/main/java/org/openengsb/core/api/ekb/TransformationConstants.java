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

package org.openengsb.core.api.ekb;

/**
 * The TransformationConstants class sums up all strings for the transformation operation parameters so that they don't
 * need to be hard-coded at three stages.
 */
public final class TransformationConstants {

    private TransformationConstants() {
    }

    /**
     * The parameter for the concat operation.
     */
    public static String concatParam = "concatString";

    /**
     * The parameter for the split operation.
     */
    public static String splitParam = "splitString";

    /**
     * The parameter for all operations where more than one result happens during the transformation (e.g. split). The
     * user than has to define which of the results should be forwarded to the target field.
     */
    public static String index = "resultIndex";

    /**
     * The parameter for the substring operation which defines from which index the substring starts.
     */
    public static String substringFrom = "from";

    /**
     * The parameter for the substring operation which defines to which index the substring goes.
     */
    public static String substringTo = "to";

    /**
     * The parameter for the value operation.
     */
    public static String value = "value";

    /**
     * The parameter for the function which returns the length of the object on the source side for the length step.
     */
    public static String lengthFunction = "function";

    /**
     * The parameter for the function which replaces some substring of a string with another string. This parameter
     * defines the string that shall be replaced.
     */
    public static String replaceOld = "oldString";

    /**
     * The parameter for the function which replaces some substring of a string with another string. This parameter
     * defines the string that shall replace the old.
     */
    public static String replaceNew = "newString";
}
