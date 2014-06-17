package org.openengsb.core.ekb.api;
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


import org.openengsb.core.api.model.QueryRequest;

/**
 * A QueryParser service is a service to parse queries for models from a string into a QueryRequest object.
 */
public interface QueryParser {

    /**
     * Returns true if the given query can be parsed by this service or false if not.
     */
    boolean isParsingPossible(String query);

    /**
     * Parses the given query string into a QueryRequest object. If the service is not able to parse the query string,
     * an EKBException is thrown.
     */
    QueryRequest parseQueryString(String query) throws EKBException;
}
