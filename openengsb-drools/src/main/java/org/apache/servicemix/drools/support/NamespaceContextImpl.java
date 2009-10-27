/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.drools.support;

import java.util.Map;

/**
 * A simple namespace context with a clean xbean configuration.
 *
 * @org.apache.xbean.XBean element="namespace-context"
 *                         description="A NamespaceContext implementation"
 * @author gnodet
 * @version $Revision: 640990 $
 */
public class NamespaceContextImpl extends org.apache.servicemix.jbi.jaxp.NamespaceContextImpl {

    /**
     * Keep the getter/setter to keep xbean annotation
     * @org.apache.xbean.Map entryName="namespace" keyName="prefix"
     * @return Returns the namespaces.
     */
    public Map<String, String> getNamespaces() {
        return super.getNamespaces();
    }

    /**
     * Keep the getter/setter to keep xbean annotation
     * @param namespaces The namespaces to set.
     */
    public void setNamespaces(Map<String, String> namespaces) {
        super.setNamespaces(namespaces);
    }

}

