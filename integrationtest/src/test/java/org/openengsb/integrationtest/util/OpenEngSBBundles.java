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

public class OpenEngSBBundles {

    public static final MavenArtifactProvisionOption OPENENGSB_WRAPPED_WICKET = CoreOptions
            .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.wrapped").artifactId(
                    "org.apache.wicket-all"));

    public static final MavenArtifactProvisionOption OPENENGSB_WRAPPED_JAXB = CoreOptions
            .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.wrapped").artifactId(
                    "javax.xml.jaxb-all"));

    public static final MavenArtifactProvisionOption OPENENGSB_WRAPPED_GUAVA = CoreOptions
            .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.wrapped").artifactId(
                    "com.google.guava-all"));

    public static final MavenArtifactProvisionOption OPENENGSB_CONFIG_JETTY = CoreOptions
            .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.config").artifactId(
                    "openengsb-config-jetty"));

    public static final MavenArtifactProvisionOption OPENENGSB_CONFIG_WEBEXTENDER = CoreOptions
            .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.config").artifactId(
                    "openengsb-config-webextender"));

    public static final MavenArtifactProvisionOption OPENENGSB_CORE_COMMON = CoreOptions
            .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.core").artifactId(
                    "openengsb-core-common"));

    public static final MavenArtifactProvisionOption OPENENGSB_CORE_WORKFLOW = CoreOptions
            .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.core").artifactId(
                    "openengsb-core-workflow"));

    public static final MavenArtifactProvisionOption OPENENGSB_DOMAINS_EXAMPLE_IMPLEMENTATION = CoreOptions
            .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.domains.example").artifactId(
                    "openengsb-domains-example-implementation"));

    public static final MavenArtifactProvisionOption OPENENGSB_DOMAINS_EXAMPLE_CONNECTOR = CoreOptions
            .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.domains.example").artifactId(
                    "openengsb-domains-example-connector"));

    public static final MavenArtifactProvisionOption OPENENGSB_DOMAINS_NOTIFICATION_IMPLEMENTATION = CoreOptions
            .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.domains.notification")
                    .artifactId("openengsb-domains-notification-implementation"));

    public static final MavenArtifactProvisionOption OPENENGSB_DOMAINS_NOTIFICATION_EMAIL = CoreOptions
            .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.domains.notification.email")
                    .artifactId("openengsb-domains-notification-email"));

    public static final MavenArtifactProvisionOption OPENENGSB_UI_WEB = CoreOptions
            .mavenBundle(new MavenArtifactUrlReference().groupId("org.openengsb.ui").artifactId("openengsb-ui-web")
                    .type("war"));

    private OpenEngSBBundles() {
        // should not be instanciable, but should be allowed to contain private
        // methods
    }
}
