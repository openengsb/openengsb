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
package org.openengsb.link.http;

import java.util.List;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.Endpoint;

/**
 * @org.apache.xbean.XBean element="linkhttpComponent"
 *                         description="edb Component" The edb-jbi-component
 *
 */
public class LinkHttpProcessComponent extends DefaultComponent {
    private Endpoint[] endpoints;

    public Endpoint[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Endpoint[] endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    protected List<?> getConfiguredEndpoints() {
        return asList(endpoints);
    }

    @Override
    protected Class<?>[] getEndpointClasses() {
        return new Class[] { Endpoint.class };
    }
}
