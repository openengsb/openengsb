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
package org.openengsb.scm;

import javax.xml.namespace.QName;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.endpoints.ForwardEndpoint;

/**
 * @org.apache.xbean.XBean element="scmEndpoint" description="The only
 *                         SCM-Domain-Endpoint. This Endpoint is responsible for
 *                         forwarding all requests to an actual connector, based
 *                         on the configuration in the context"
 */
public class ScmEndpoint extends ForwardEndpoint<Object> {

    @Override
    protected QName getForwardTarget(ContextHelper contextHelper) {
        String defaultName = contextHelper.getValue("scm/default");
        String serviceName = contextHelper.getValue("scm/" + defaultName + "/servicename");
        String namespace = contextHelper.getValue("scm/" + defaultName + "/namespace");
        return new QName(namespace, serviceName);
    }

}
