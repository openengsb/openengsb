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

package org.openengsb.itests.util;

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.ops4j.pax.exam.OptionUtils.combine;

import org.apache.karaf.tooling.exam.options.configs.FeaturesCfg;
import org.openengsb.domain.auditing.AuditingDomain;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;

/**
 * This additional layer is required since the Configuration annotation is differently handled in exam 2.x. To avoid any
 * ambitious behavior tests which need to modify the base options should rather direclty extend the
 * {@link AbstractExamTestHelper} class instead of this one.
 */
public abstract class AbstractPreConfiguredExamTestHelper extends AbstractExamTestHelper {

    @Configuration
    public static Option[] configuration() throws Exception {
        return combine(baseConfiguration(),
            editConfigurationFileExtend(FeaturesCfg.BOOT, ",openengsb-connector-example"));
    }

    protected void waitForAuthenticationEnvironment() throws Exception {
        getOsgiService(AuditingDomain.class, 30000);
        getOsgiService(AuthenticationDomain.class, "(service.pid=root-authenticator)", 30000);
        getOsgiService(AuthorizationDomain.class, "(service.pid=root-authorizer)", 30000);

        // TODO: find better solution to find out if the Shiro authenticator is running
        waitForOsgiBundle("org.openengsb.framework.services");
    }

}
