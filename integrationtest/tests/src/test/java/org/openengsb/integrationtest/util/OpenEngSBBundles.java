/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.integrationtest.util;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;

public final class OpenEngSBBundles {

    public static final MavenArtifactProvisionOption OPENENGSB_WRAPPED_WICKET = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.wrapped")
            .artifactId("org.apache.wicket-all").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_WRAPPED_JAXB = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.wrapped").artifactId("javax.xml.jaxb-all")
            .version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_WRAPPED_GUAVA = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.wrapped")
            .artifactId("com.google.guava-all").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_WRAPPED_XMLRPC = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.wrapped")
            .artifactId("org.apache.xmlrpc-all").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_WRAPPED_JSCH = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.wrapped").artifactId("com.jcraft.jsch-all")
            .version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_WRAPPED_NEODATIS = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.wrapped")
            .artifactId("org.neodatis.odb-all").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_INTEGRATIONTEST_WRAPPED_HTMLUNIT = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.integrationtest.wrapped")
            .artifactId("net.sourceforge.htmlunit-all").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_CORE_COMMON = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.core").artifactId("openengsb-core-common")
            .version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_CORE_EVENTS = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.core").artifactId("openengsb-core-events")
            .version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_CORE_PERSISTENCE = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.core")
            .artifactId("openengsb-core-persistence").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_CORE_WORKFLOW = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.core")
            .artifactId("openengsb-core-workflow").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_CORE_TASKBOX = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.core").artifactId("openengsb-core-taskbox")
            .version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_CORE_SECURITY = CoreOptions
          .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.core")
                          .artifactId("openengsb-core-security").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_CORE_USERMANAGEMENT = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.core")
            .artifactId("openengsb-core-usermanagement").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_DOMAIN_EXAMPLE = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.domain")
            .artifactId("openengsb-domain-example").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_CONNECTOR_EXAMPLE = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.connector")
            .artifactId("openengsb-connector-example").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_DOMAIN_AUDITING = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.domain")
            .artifactId("openengsb-domain-auditing").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_CONNECTOR_AUDITING = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.connector")
            .artifactId("openengsb-connector-memory-auditing").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_UI_COMMON_WICKET = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.ui.common")
            .artifactId("openengsb-ui-common-wicket").version(new OpenEngSBVersionResolver()));

    public static final MavenArtifactProvisionOption OPENENGSB_UI_WEB = CoreOptions
        .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.ui").artifactId("openengsb-ui-web")
            .type("war").version(new OpenEngSBVersionResolver()));

    private OpenEngSBBundles() {
        // should not be instanciable, but should be allowed to contain private
        // methods
    }
}
