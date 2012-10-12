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

package org.openengsb.core.ekb.api.transformation;

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
    public static final String CONCAT_PARAM = "concatString";

    /**
     * The parameter for the split operation.
     */
    public static final String SPLIT_PARAM = "splitString";
    
    /**
     * The parameter for all operations that take a regular expression parameter.
     */
    public static final String REGEX_PARAM = "regexString";

    /**
     * The parameter for all operations where more than one result happens during the transformation (e.g. split). The
     * user than has to define which of the results should be forwarded to the target field.
     */
    public static final String INDEX_PARAM = "resultIndex";

    /**
     * The parameter for the substring operation which defines from which index the substring starts.
     */
    public static final String SUBSTRING_FROM_PARAM = "from";

    /**
     * The parameter for the substring operation which defines to which index the substring goes.
     */
    public static final String SUBSTRING_TO_PARAM = "to";

    /**
     * The parameter for the value operation.
     */
    public static final String VALUE_PARAM = "value";

    /**
     * The parameter for the function which returns the length of the object on the source side for the length step.
     */
    public static final String LENGTH_FUNCTION_PARAM = "function";

    /**
     * The parameter for the function which replaces some substring of a string with another string. This parameter
     * defines the string that shall be replaced.
     */
    public static final String REPLACE_OLD_PARAM = "oldString";

    /**
     * The parameter for the function which replaces some substring of a string with another string. This parameter
     * defines the string that shall replace the old.
     */
    public static final String REPLACE_NEW_PARAM = "newString";

    /**
     * The parameter for the pad function which specifies how far the padding should be done.
     */
    public static final String PAD_LENGTH_PARAM = "length";

    /**
     * The parameter for the pad function which specifies which character should be used for the padding.
     */
    public static final String PAD_CHARACTER_PARAM = "char";

    /**
     * The parameter for the pad function which specifies if the padding should be done at the beginning or the end of
     * the string.
     */
    public static final String PAD_DIRECTION_PARAM = "direction";

    /**
     * The parameter for the remove leading operation which defines how long at maximum the deletion should be done.
     */
    public static final String REMOVELEADING_LENGTH_PARAM = "length";
    
    /**
     * This parameter for the instantiate operation defines which object type should be instantiated.
     */
    public static final String INSTANTIATE_TARGETTYPE_PARAM = "targetType";
    
    /**
     * This parameter for the instantiate operation defines which method should be used initiate the object. If empty
     * the constructor will be used.
     */
    public static final String INSTANTIATE_INITMETHOD_PARAM = "targetTypeInit";
}
