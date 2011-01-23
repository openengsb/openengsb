openengsb-1.1.0.RELEASE
-----------------------

### Highlights
  * Downgrade spring-dm to 1.x and switch to blueprint instead of spring
  * Added tooling plugin to execute license-check plugin and assemble scripts via mvn.
  * Added possibility to brand konsole
  * renamed: maven-openengsb-plugin -> openengsb-maven-plugin
  * OpenEngSB dependencies could be used now by scope import of openengsb poms/pom.xml
  * openengsb-config-* and provision removed by upgrade to karaf-2.x model
  * added human workflow
  * added jira connector
  * added usermanagement
  * added maven plugin to handle typical goals for openengsb and child projects
  * workflow-service: remove droolsHelper and use drools-provided methods for starting workflows in rule-consequences (used in registerEventForFlow)
  * connector-email: automatically add a space after the prefix.
  * restructure Event-Hierarchy: xEndEvent -> xSuccessEvent and xFailEvent (for deploy, build and test)
  * add "processId"-field to event. Events where this property is set are only signaled to this process instead of all of them (they are still inserted into the workingmemory for the global rules).
  * Domains build, test, and deploy now provide additional methods that take a processId as argument. All events that are raised in these methods are only signaled to this processId.
  * Plaintext-report now uses getter methods of Events rather than direct reflection on the fields for report generation.
  * improve logging in workflow engine
  * when starting a flow, the ProcessInstance is now inserted into the workingmemory for use inside the workflow.
  * startFlow now triggers a "FlowStartedEvent" as soon as the processInstance has been inserted.
  * workflow-service: retract events from working-memory when all rules have fired.

### Details

openengsb-1.0.0.RELEASE
-----------------------

Initial release

### Details

Release Notes - OpenEngSB - Version openengsb-1.0.0.RELEASE

#### Bug
    * [OPENENGSB-8] - maven-jibx-plugin creates windows type line endings if used on windows
    * [OPENENGSB-19] - openengsb-domains-notification-xmpp-se fails at will (see e.g. hudson build #243).
    * [OPENENGSB-32] - docbkx html-multi doesn't work
    * [OPENENGSB-38] - Check dependencies of embedded project
    * [OPENENGSB-39] - replace tmatesoft
    * [OPENENGSB-65] - make docbkx textobjects work with pdf/html
    * [OPENENGSB-69] - webbrowser shows exception after link-request
    * [OPENENGSB-73] - nullointer exception in CIT use case
    * [OPENENGSB-81] - Links to manual on homepage broken
    * [OPENENGSB-82] - 3rd party repository seems broken
    * [OPENENGSB-106] - Rat license plugin have to be replaced
    * [OPENENGSB-116] - Avoid UI crashes
    * [OPENENGSB-174] - Wicket pages expire too fast
    * [OPENENGSB-180] - fix workflow integration test "testSendEvent"
    * [OPENENGSB-184] - Language change is not working when editor is active
    * [OPENENGSB-205] - Rulemanager allows to add broken rules but doesn't come up again if a broken rule exsists
    * [OPENENGSB-211] - Windows Console does not interact with users
    * [OPENENGSB-252] - checkstyle and eclipse formater disagree about array construction (whitespace after { )
    * [OPENENGSB-253] - It is not possible to start the OpenEngSB without a network
    * [OPENENGSB-277] - Bypassing validation in Service-editor
    * [OPENENGSB-284] - release behaves strangely?!
    * [OPENENGSB-285] - changing receiver in rule fails
    * [OPENENGSB-286] - Integration test (WorkflowIT) fails sometimes, because of missing globals. Maybe it is a timing issue, because the (EventForwardIT), which basically performs the same operation on the workflow service succeeds.
    * [OPENENGSB-287] - mvn pax:provision fails in Windows because of space in release name (eg. Dashing Donald fails, Dashing_Donald works)
    * [OPENENGSB-295] - Testclient doesn't show parameters for Connector or Domain when calling method
    * [OPENENGSB-297] - build fails on build.openengsb.org because of windows tmp file problem
    * [OPENENGSB-298] - Send event page contains Checkbox "validate service"
    * [OPENENGSB-301] - domains are not localized correctly on test-client
    * [OPENENGSB-309] - wicket localisation problem shown during tests
    * [OPENENGSB-310] - wicket ../ used for path navigation
    * [OPENENGSB-314] - Fix wrong handling of private vars in event
    * [OPENENGSB-324] - Problem on startup of final product with the help of the karaf.bat script.
    * [OPENENGSB-326] - all components should store in ${karaf.data} system var instead of ./data/
    * [OPENENGSB-370] - Its not granted that all spring-configs are up and running when the integration tests start. This can lead to stucks or other strange errors.
    * [OPENENGSB-379] - maven-licence-plugin should accept licence header in xml when below the processing instruction.
    * [OPENENGSB-383] - NotSerializableException for BundleStrings in LocalizableStringModel
    * [OPENENGSB-387] - ClassNotFound Error during shutdown integration tests
    * [OPENENGSB-396] - Workflow description in usermanual has some rendering errors
    * [OPENENGSB-399] - Scm domain description is not shown on index page of the OpenEngSB if language is set to german. Maybe the description is missing, or the translation is missing.
    * [OPENENGSB-400] - although creating service fails, service will be created
    * [OPENENGSB-401] - Using Firefox under Windows (German) the space between Emerald Eve is not shown corretly.
    * [OPENENGSB-402] - nullpointer exception after creating some services
    * [OPENENGSB-403] - Page is not rendered correctly using Internet Explorer
    * [OPENENGSB-404] - No error message using wrong credentials
    * [OPENENGSB-405] - trac connector - fix create issue 
    * [OPENENGSB-406] - trac connector - delete issue input field for param is missing
    * [OPENENGSB-409] - Password-field empty in service-editor
    * [OPENENGSB-410] - Submit-button in "edit service"-page should be named "save" instead of "Create"
    * [OPENENGSB-419] - replace current object database with neodatis
    * [OPENENGSB-425] - keep double qoute and distance during release
    * [OPENENGSB-426] - persistence unit uses system out anywhere
    * [OPENENGSB-429] - service restore does not work
    * [OPENENGSB-448] - hudson build server unstable
    * [OPENENGSB-465] - Development-links in homepage are not generated
    * [OPENENGSB-468] - Fix ProxyServiceManager NotSerializableException
    * [OPENENGSB-492] - docbook <link linked=ID>bla</link> attribute is not rendered correctly
    * [OPENENGSB-498] - documentation is not pushed correctly to mvn site (not up-to-date)
    * [OPENENGSB-511] - move activemq-data to ${karaf.data} directory
    * [OPENENGSB-512] - favicon is shown after login but not before
    * [OPENENGSB-513] - Internet Explorer Interprets CSS wrong (see screenshot)
    * [OPENENGSB-514] - Apostrophs are not shown in the webapp for release names
    * [OPENENGSB-522] - userguide link on main page links to old documentation
    * [OPENENGSB-532] - Edit Button in Testclient is very hacky. NotSerialisationException is thrown because last used ServiceManager is stored directly and not resolved via an id.
Edit button should be completely redone
    * [OPENENGSB-533] - JsonMappingException: Unrecognized field "type" (Class org.openengsb.core.common.Event), not marked as ignorable
    * [OPENENGSB-534] - java.lang.NoSuchMethodException thrown on EventCaller.raiseEvent call
    * [OPENENGSB-542] - Update links to interface in documentation for all domains to new domain and connector project structure.
    * [OPENENGSB-546] - using localhost:8090/openengsb from multible browsers at the same time
    * [OPENENGSB-547] - Internet Explorer Renders Flags and Header strangely
    * [OPENENGSB-548] - jetty7 - felix problems 
    * [OPENENGSB-551] - OpenEngSB release package shows "null null" for version name
    * [OPENENGSB-555] - Update ConnectorSetupStore to multi-domain connectors
    * [OPENENGSB-557] - LICENSE & Distribution files are not included into final distribution
    * [OPENENGSB-574] - Release versions of integration part are not increased
    * [OPENENGSB-595] - Value is missing as default import in drools
    * [OPENENGSB-605] - Use png as favicon for openengsb war file and script
    * [OPENENGSB-616] - broken links in documentation
    * [OPENENGSB-619] - Maven connector has to read output and error stream from process in a separate thread

#### Epic
    * [OPENENGSB-281] - Dedicated CIT server (OpenCIT) based on the OpenEngSB, building the OpenEngSB

#### Improvement
    * [OPENENGSB-2] - Allow to use the mvn cli plugin for easier use
    * [OPENENGSB-3] - Drools DirectorySource should support multiple Importfiles
    * [OPENENGSB-4] - embed eXist-database
    * [OPENENGSB-5] - make exist-util the unified storage-solution for endpoints
    * [OPENENGSB-9] - Use java.nio to watch the rulebase-folder when migrating to java 7
    * [OPENENGSB-10] - mvn site deploy should use scp instead of file as deployment prototcol
    * [OPENENGSB-13] - Optimize build process by separating build processes with maven profiles
    * [OPENENGSB-14] - Increase artifact management and organisation by using different groupIds for the artifacts
    * [OPENENGSB-15] - Make maven repositories unique named to make it easier for developer to identify them unique in their settings.xml files
    * [OPENENGSB-21] - Add OSGi Header to SE-JBI artifacts
    * [OPENENGSB-22] - Cleanup and unify connectors and domains
    * [OPENENGSB-24] - Create endpoint infrastructure for producer-only endpoints.
    * [OPENENGSB-36] - Facebook notification connector
    * [OPENENGSB-40] - simply eventHelper.sendEvent method
    * [OPENENGSB-47] - domain proposal: cleanup test interface
    * [OPENENGSB-50] - Make context editable in web ui
    * [OPENENGSB-56] - enhance Build, test, deploy domain methods return value
    * [OPENENGSB-80] - Simplify pom structure by using properties for versions instead of dependency management
    * [OPENENGSB-84] - Port context viewer into wicket testapp
    * [OPENENGSB-90] - route service calls through domain
    * [OPENENGSB-115] - Basic navigation
    * [OPENENGSB-122] - maven-bundle-plugin cannot handle jaxb-xjc
    * [OPENENGSB-126] - upgrade spring to 3.0.4.RELEASE
    * [OPENENGSB-127] - Upgrade karaf to 2.0.0
    * [OPENENGSB-134] - Workflow-component should set domain-helper-globals dynamically
    * [OPENENGSB-136] - non-required support for dropdown choice in editor
    * [OPENENGSB-137] - create context entries
    * [OPENENGSB-138] - Improve Testclient usability (serialize)
    * [OPENENGSB-139] - Fine-tune maven-license-plugin
    * [OPENENGSB-146] - Improve Testclient usability
    * [OPENENGSB-149] - improve Context-editor usability
    * [OPENENGSB-151] - Current context(-id) should be saved per session (in a model), and should be selectable in a combobox
    * [OPENENGSB-152] - provide a checkstyle-configuration file for developer
    * [OPENENGSB-173] - set variables in own startup scripts
    * [OPENENGSB-175] - http://openengsb.org/howto/howto_contexteditor.html is unclear
    * [OPENENGSB-176] - Log output in example event is not specified clear enough
    * [OPENENGSB-182] - RuleManager-Service should not declare Checked Exception in every method (e.g. list).
    * [OPENENGSB-183] - Show version in UI (at release, in site & in openengsb product page)
    * [OPENENGSB-189] - Generic implementation of Domain ForwardService, to automatically forward any methodcall to the default-connector
    * [OPENENGSB-202] - package configuration files for client (related to OPENENGSB-102)
    * [OPENENGSB-212] - Checkstyle maven profile
    * [OPENENGSB-237] - External Links from Homepage should open new Tab
    * [OPENENGSB-238] - Attach ReadMe, NOTICE and LICENSE File to Distribution
    * [OPENENGSB-239] - Release shows debug statements and *lots* of exceptions
    * [OPENENGSB-240] - Improve Internationalisation
    * [OPENENGSB-241] - Quickstart Pages should be connected
    * [OPENENGSB-242] - Significantly improve HowTo (First Steps)
    * [OPENENGSB-243] - Improve Navigation
    * [OPENENGSB-244] - Fix Form validation
    * [OPENENGSB-245] - Service Creation Feedback
    * [OPENENGSB-251] - do string localization at actual query time, not construction time
    * [OPENENGSB-254] - Supress log-output on console
    * [OPENENGSB-259] - Supress Auto-properties start on karaf console
    * [OPENENGSB-272] - validate services before creation and update 
    * [OPENENGSB-282] - Move Proxy Connector from JMS-Domain to more general subproject
    * [OPENENGSB-283] - ajax feedback after pressing send in testclient
    * [OPENENGSB-294] - increase page timout
to something like an hour
    * [OPENENGSB-296] - Consider bringing ProxyServiceManager and AbstractServiceManager together to further code reuse.
    * [OPENENGSB-299] - SendEvent-page should indicate activity while processing the event.
    * [OPENENGSB-304] - deactivate breadcrumbs on all wicket pages
    * [OPENENGSB-311] - Use the same UI theme for login as for the openengsb
    * [OPENENGSB-313] - use detachable model for contex service in wicket base page
    * [OPENENGSB-320] - Wrap services on Test-Client-Page into the belonging domains
    * [OPENENGSB-321] - keep treeview on UI open by default
    * [OPENENGSB-322] - replace project context selector and logout button
    * [OPENENGSB-328] - Run configuration contains openengsb-specific settings
    * [OPENENGSB-372] - Upgrade maven to 3.0
    * [OPENENGSB-385] - add additional pre-push script with variable entry point
    * [OPENENGSB-386] - Make jms connector port variable in a startup config file
    * [OPENENGSB-388] - use better names for polymorphic jms services
    * [OPENENGSB-393] - Use openengsb release version and name in user documentation
    * [OPENENGSB-413] - after creating service, jump to new service in tree and load methods
    * [OPENENGSB-438] - Example Connector should throw LogEvent after logging.
    * [OPENENGSB-439] - Review and improve sponsor page on homepage (company logos, etc...)
    * [OPENENGSB-442] - FileSystemReportStore should support any file names, including those which cannot be used as filenames.
    * [OPENENGSB-444] - provide htmlunit for integration test internal
    * [OPENENGSB-450] - restructure manual
    * [OPENENGSB-461] - Add drools-flow documentation in the manual.
    * [OPENENGSB-462] - Document context in manual
    * [OPENENGSB-463] - issue domain interface id type miss match
    * [OPENENGSB-466] - Remove runner before starting maven build in run.sh script
    * [OPENENGSB-467] - Test navigation by clicking every navigation link via htmlunit. If the server crashes the IntegrationTest fails.
    * [OPENENGSB-497] - use correct tag/link in documentation to src
    * [OPENENGSB-515] - Pages show now titles
    * [OPENENGSB-519] - put all interfaces that define the public api of the openengsb core into the common project.
    * [OPENENGSB-521] - Split connectors and domains to separate folders
    * [OPENENGSB-536] - add debug openengsb to FAQ on HP and documentation
    * [OPENENGSB-556] - Allow client projects usage of AOP
    * [OPENENGSB-558] - add nightly profile to assembly script
    * [OPENENGSB-561] - changes for OPENENGSB-195/wizzard
    * [OPENENGSB-576] - Improve online documentation - broken links
    * [OPENENGSB-580] - Upgrade changelog and documentation
    * [OPENENGSB-581] - extend documentation at chapter contributor workflow
    * [OPENENGSB-586] - maven tries to download missing artifacts
    * [OPENENGSB-603] - Context has to be stored persistently and restored on system startup
    * [OPENENGSB-610] - Maven connector has to support the execution of a configurable command

#### Infrastructure
    * [OPENENGSB-380] - Create layout for PDF Export
    * [OPENENGSB-496] - Upgrade jira and greenhopper

#### New Feature
    * [OPENENGSB-1] - As a developer I want to directly start the OpenEngSB via maven
    * [OPENENGSB-6] - Add and configure clover2 for the OpenEngSB and Hudson
    * [OPENENGSB-16] - Add subproject to handle and release wrapped OSGi resources such as wicket, which are currently not available osgi bundled
    * [OPENENGSB-17] - Develop facebook notification connector
    * [OPENENGSB-20] - Develop Git scm connector
    * [OPENENGSB-28] - Simplify deployment of OpenEngSB components
    * [OPENENGSB-34] - Cross protocol support (e.g. Styx) - Protocol Connectors
    * [OPENENGSB-75] - Create "debug-endpoint" with "general" interface wich does nothing but writes output to console
    * [OPENENGSB-76] - remote interface for persistence core component
    * [OPENENGSB-77] - implement OPENENGSB-76 to access embedded eXist-storage
    * [OPENENGSB-92] - Implement workflow engine based on servicemix workflow-se
    * [OPENENGSB-93] - Implement unified storage solution for all connectors and config, using eXist-xmldb (based on persistence-se from servicemix)
    * [OPENENGSB-94] - Implement notification-domain based on the corresponding servicemix-artifact
    * [OPENENGSB-95] - Implement issue-domain based on the corresponding servicemix-artifact
    * [OPENENGSB-96] - Implement primitive continuous integration usecase (scm, deploy, report, build, test)
    * [OPENENGSB-97] - Implement JMS-Connector for providing domain services
    * [OPENENGSB-98] - Support to modify existing serivce instances
    * [OPENENGSB-99] - Attribute validation for service instance editor
    * [OPENENGSB-100] - Service-properties should support datatypes.
    * [OPENENGSB-101] - Security: The openengsb config-ui must require a login.
    * [OPENENGSB-102] - Plugin system for 3rd party clients
    * [OPENENGSB-103] - Use exam to create integration tests [continuous integration]
    * [OPENENGSB-104] - introduce exam to provide good means for debugging for devs
    * [OPENENGSB-131] - Use Drools to choose correct connector depending on context and message
    * [OPENENGSB-132] - message should contain receiver connector
    * [OPENENGSB-144] - Rule Management with rule editor
    * [OPENENGSB-147] - Validation of Connectors (e.g. is email-address valid)
    * [OPENENGSB-148] - Subject-prefix for notification-domain
    * [OPENENGSB-150] - create Eventmanagement-UI
    * [OPENENGSB-172] - Service Descriptor and UI have to support connector services attributes
    * [OPENENGSB-185] - Support manual editing of drools flow files like rules are supported currently (1.0.0.M2)
    * [OPENENGSB-187] - Text based rule editor should support rule flows as well.
    * [OPENENGSB-188] - Migrate issue domain (only create issue method) and trac connector
    * [OPENENGSB-190] - Develop/Use Plugin-System for Wicket-Pages
    * [OPENENGSB-192] - report generation component
    * [OPENENGSB-193] - Archiving and presentation of multiple, generated reports
    * [OPENENGSB-206] - Trac Connector and Issue Domain
    * [OPENENGSB-213] - Rule manager service has to support drools flows
    * [OPENENGSB-216] - Domain event interface
    * [OPENENGSB-268] - Flow infrastructure
    * [OPENENGSB-289] - script to add license to files
    * [OPENENGSB-327] - using html unit
    * [OPENENGSB-362] - Configure all ports required by the OpenEngSB in one ports.conf file
    * [OPENENGSB-407] - test client - provide dropdown for enum attributes
    * [OPENENGSB-434] - Deploy domain
    * [OPENENGSB-435] - maven deploy connector
    * [OPENENGSB-440] - The workflow engine hast to support the possibility to poll the state of a workflow.
    * [OPENENGSB-443] - Trigger workflows by events
    * [OPENENGSB-452] - Rulemanager or workflow engine has to provide a possibility to manually add globals.
    * [OPENENGSB-457] - Create taskbox core component


#### Task
    * [OPENENGSB-7] - Move "Documentation" from openengsb.org HP to the Usermanual
    * [OPENENGSB-11] - Add OpenEngSB admin section and explain what is relevant for sub-projects
    * [OPENENGSB-12] - Document relevant information for administrator and persons with all passwords to access the OpenEngSB infrastructure
    * [OPENENGSB-25] - Clearly separate implementation and configuration in xbean files via properties
    * [OPENENGSB-26] - review facebook-notification-connector
    * [OPENENGSB-27] - Describe currently possible use-cases on the HP and how they can be described
    * [OPENENGSB-30] - Move testapp client into wicket web-ui
    * [OPENENGSB-31] - port core/config-ui client into wicket web-ui
    * [OPENENGSB-33] - Describe "When to use OpenEngSB"
    * [OPENENGSB-43] - Testmonitor for OpenEngSB / IsAlive check for 
    * [OPENENGSB-44] - document supported encodings
    * [OPENENGSB-45] - add automatic build
    * [OPENENGSB-46] - domain proposal: add tagging to scm domain
    * [OPENENGSB-48] - domain proposal: extend pim-domain
    * [OPENENGSB-49] - domain proposal: add modeling
    * [OPENENGSB-51] - Remove embedded from build cycle
    * [OPENENGSB-54] - Update issue domain proposal
    * [OPENENGSB-55] - create integration tests for the svn tool connector
    * [OPENENGSB-59] - add basic architectural documentation
    * [OPENENGSB-62] - make mail connector testable
    * [OPENENGSB-71] - Paths within servicemix-installation are very long (too long)
    * [OPENENGSB-74] - File domain motivations
    * [OPENENGSB-79] - port apt connector & domain docu to docbook
    * [OPENENGSB-87] - Revalidate context store in OSGi context.
    * [OPENENGSB-88] - OpenEngSB connector instances have to be restored at restart
    * [OPENENGSB-105] - Upgrade wicket 1.4.9 to 1.4.10
    * [OPENENGSB-107] - Update openengsb maven repository
    * [OPENENGSB-121] - wrap google's guava-libraries
    * [OPENENGSB-140] - Place milestone name anywhere on site?!
    * [OPENENGSB-141] - Cleanup maven repositories
    * [OPENENGSB-145] - Cleanup no longer required projects such as packages
    * [OPENENGSB-167] - merge config and common project
    * [OPENENGSB-177] - refactor service manager related code
    * [OPENENGSB-178] - archetype for domain
    * [OPENENGSB-179] - archetype for connector
    * [OPENENGSB-194] - CIT flow & required rules
    * [OPENENGSB-195] - CIT flow wizzard setup
    * [OPENENGSB-196] - git connector
    * [OPENENGSB-197] - maven build connector
    * [OPENENGSB-198] - maven test connector
    * [OPENENGSB-201] - extract abstract base class from DomainProvider
    * [OPENENGSB-203] - Upgrade felix osgi bundles to latest versions
    * [OPENENGSB-204] - Upgrade pax runner and pax exam
    * [OPENENGSB-207] - Create archetype for third party plugin system
    * [OPENENGSB-208] - Cleanup License Check (Warnings, not found, config files)
    * [OPENENGSB-209] - Look into olink creation with docbook
    * [OPENENGSB-210] - add own maven site skin to a) make it adaptable and b) show version on openengsb.org (OPENENGSB-183)
    * [OPENENGSB-214] - Document forward service
    * [OPENENGSB-215] - Stub for documentation of domains and connectors
    * [OPENENGSB-217] - create howto description for domain creation
    * [OPENENGSB-218] - document how rules are managed with the help of the RuleManager
    * [OPENENGSB-219] - Add schema location to docbook files
    * [OPENENGSB-220] - Document architecture and howto use for ServiceManager
    * [OPENENGSB-221] - Update developer guide
    * [OPENENGSB-222] - Delete runtime files of integration test in tmp folder
    * [OPENENGSB-223] - readme about useful eclipse settings.
    * [OPENENGSB-227] - Remove pax output during tests
    * [OPENENGSB-232] - fix warning in field validator in EditorPanel
    * [OPENENGSB-233] - check for existing connector dir when executing mv in gen-connector.sh
    * [OPENENGSB-234] - adapt connector archetype to new group/namespace naming schema
    * [OPENENGSB-235] - provide unit tests for org.openengsb.ui.web.service.impl.*
    * [OPENENGSB-246] - Checkstyle: remove rule: Method-name must match pattern '^[a-z][a-zA-Z0-9]*$'.
    * [OPENENGSB-247] - Checkstyle: formatter and checkstyle disagree about array-indention-level
    * [OPENENGSB-248] - Checkstyle: remove rule that prohibits public and protected variables
    * [OPENENGSB-249] - Checkstyle: formatter and checkstyle disagree about whitespace after '{'
    * [OPENENGSB-256] - Upgrade wicket 1.4.10 to 1.4.12
    * [OPENENGSB-262] - Add manual (html + pdf format) to distribution
    * [OPENENGSB-263] - add contribution section to hp and README
    * [OPENENGSB-265] - upgrade drools 5.1.0->5.1.1
    * [OPENENGSB-266] - Checkstyle: should allow protected and public members
    * [OPENENGSB-269] - lookup validator localizations through bundlestrings (interface)
    * [OPENENGSB-273] - Set wicket test logging output to warn
    * [OPENENGSB-274] - Architecture Documentation
    * [OPENENGSB-276] - Refactor and document rule manager
    * [OPENENGSB-278] - Checkstyle: Allow if without '{'
    * [OPENENGSB-288] - provide example files for each license file
    * [OPENENGSB-290] - New Checkstyle Rules: enable import-order-rule, newline at end of file, Translation, uncommented Main
    * [OPENENGSB-292] - Document IRC channel on HP
    * [OPENENGSB-302] - release name for openengsb-1.0.0.M6
    * [OPENENGSB-303] - hudson should create snapshots (+ nightly doc) from master, and only test integration branch
    * [OPENENGSB-305] - maven-license-plugin for README files
    * [OPENENGSB-306] - Cleanup openengsb homepage from APT parsing warnings
    * [OPENENGSB-307] - cleanup report generation deprication warning from build process
    * [OPENENGSB-312] - use digit-only child ids in wicket repeater
    * [OPENENGSB-317] - Find a workaround for openengsb third-party maven repository
    * [OPENENGSB-318] - Reevaluate repositories in the OpenEngSB (root pom.xml)
    * [OPENENGSB-319] - use fav-icon on openengsb page
    * [OPENENGSB-329] - Sketch OpenEngSB Architecture
    * [OPENENGSB-331] - add script to simply find out about version upgrades
    * [OPENENGSB-337] - upgrade maven site plugin 2.0-beta-7 to 3.0-beta-2
    * [OPENENGSB-346] - upgrade aries blueprint from 0.1-incubation to 0.2-incubation
    * [OPENENGSB-347] - upgrade felix from 3.0.2 to 3.0.3
    * [OPENENGSB-348] - upgrade apache mina from 2.0.0-RC1 to 2.0.0
    * [OPENENGSB-349] - upgrade org.apache.servicemix.bundles:org.apache.servicemix.bundles.aopalliance from 1.0_3 to 1.0_4
    * [OPENENGSB-350] - upgrade  org.apache.servicemix.bundles:org.apache.servicemix.bundles.cglib from 2.1._3_4 to 2.1_3_6
    * [OPENENGSB-351] - upgrade jetty from 6.1.20 to 6.1.25 and remove duplicated entry in poms/pom.xml
    * [OPENENGSB-352] - upgrade com.google.guava from r06 to r07
    * [OPENENGSB-353] - upgrade junit 4.7 zo 4.8.1
    * [OPENENGSB-354] - upgrade slf4j from 1.5.8 to 1.6.1
    * [OPENENGSB-356] - upgrade pax swissbox from 1.2.0 to 1.3.0
    * [OPENENGSB-357] - Evaluate if jetty 7 is already usable
    * [OPENENGSB-358] - Define Roadmap using Jira and Epics 
    * [OPENENGSB-359] - Changelog and update description
    * [OPENENGSB-368] - remove generated by maven logo from openengsb.org
    * [OPENENGSB-371] - Event generation guidelines
    * [OPENENGSB-373] - move archetypes to tooling
    * [OPENENGSB-374] - Create templates for domain and connector documentation and document already available domains and connectors.
    * [OPENENGSB-381] - corrected deprecated variables of maven3
    * [OPENENGSB-382] - Checkstyle new line at end of file
    * [OPENENGSB-384] - Move testcode for CIT flow & required rules from test to impl as domains are implemented
    * [OPENENGSB-389] - homepage and documentation update process
    * [OPENENGSB-391] - Set log-level of all tests to WARN
    * [OPENENGSB-392] - FAQ of HP is absolutely outdated
    * [OPENENGSB-394] - Rename user part of manual to something more developer specific
    * [OPENENGSB-395] - Unify domain documentation for all connectors and domains
    * [OPENENGSB-397] - Bring Documentation up-to-date
    * [OPENENGSB-412] - include docs profile for default
    * [OPENENGSB-415] - register CIT rule during startup in opencit
    * [OPENENGSB-416] - Create Wicket-UI infrastructure for OpenCIT
    * [OPENENGSB-417] - very simple opencit core ui
    * [OPENENGSB-418] - Create one jms remote log domain in any language except java
    * [OPENENGSB-421] - replace windows build server with linux build server
    * [OPENENGSB-423] - deploy final releases on maven repository
    * [OPENENGSB-427] - Maven eclipse plugin should use local formatter
    * [OPENENGSB-431] - Fill index page with some text
    * [OPENENGSB-432] - prepare openengsb maven files for upload to maven central repo
    * [OPENENGSB-436] - Include deploy into opencit process
    * [OPENENGSB-441] - code cleanup
    * [OPENENGSB-447] - Shouldn't html documentation be removed from distribution
    * [OPENENGSB-451] - update scripts
    * [OPENENGSB-453] - Provide release manager section in manual
    * [OPENENGSB-456] - Update CIT workflow or add rules to correctly update the project state when CIT workflow it is started or finished.
    * [OPENENGSB-458] - test project for common things like dependencies, dummy implementations, ...
    * [OPENENGSB-460] - Write Domain/Connector creation documentation
    * [OPENENGSB-464] - developer.wrapped.workflow in Chapter16 needs rewriting, as it is hard to understand
    * [OPENENGSB-469] - Update OpenCIT workflow when Build, Test and Deploy Domain are finished. Handle deploy in the same way build and test are handled.
    * [OPENENGSB-470] - Update OpenCIT configuration after build, test, deploy domain is finished. Especially set the globals and imports correctly.
    * [OPENENGSB-479] - Describe release process
    * [OPENENGSB-480] - Document OpenEngSB Platform for clients and admins
    * [OPENENGSB-488] - Remove Changelog from homepage
    * [OPENENGSB-489] - Add documentation section: how to read this document
    * [OPENENGSB-493] - Upgrade spring to 3.0.5.RELEASE
    * [OPENENGSB-494] - Upgrade spring security to 3.0.4.RELEASE
    * [OPENENGSB-495] - Move old nonjava projects into doc
    * [OPENENGSB-499] - Use same <link> annotation for all parts of manual
    * [OPENENGSB-508] - git irc reporting
    * [OPENENGSB-516] - Create own release target for RCs
    * [OPENENGSB-518] - Document where to upgrade logo
    * [OPENENGSB-523] - deploy sources and jdoc with snapshopts
    * [OPENENGSB-524] - Add section for recommended ecipse plugins
    * [OPENENGSB-560] - Remove all default entries from rulebase of workflow component.
    * [OPENENGSB-564] - upgrade maven-war-plugin to 2.1.1
    * [OPENENGSB-570] - Provide openengsb-webpage baseclass for client-webinterfaces
    * [OPENENGSB-573] - finish release scripts for different types
    * [OPENENGSB-594] - document how a pom in the openengsb project should look like
    * [OPENENGSB-606] - update docs new jira release 

#### TBD
    * [OPENENGSB-23] - JGit merge not yet usable
    * [OPENENGSB-325] - Use Flags instead of own list for impediments
    * [OPENENGSB-365] - Add issue update event to notification schema
    * [OPENENGSB-367] - Event Interface with several overloaded methods is messy, as external System has to provide metadata which type should be used for serialisation and identification of correct overloaded method. Maybe it is the only solution, but still feels a little messy
    * [OPENENGSB-398] - release name for openengsb-1.0.0.RC1
    * [OPENENGSB-420] - license approval process
    * [OPENENGSB-424] - Release candiates and repo structure
    * [OPENENGSB-430] - close issues after they are merged
    * [OPENENGSB-437] - Use Link to link issues!
    * [OPENENGSB-446] - How to mark pull requests that are currently being reviewed by somebody. Wouldn't want 2 people reviewing the same pull request.
    * [OPENENGSB-455] - Version name schema
    * [OPENENGSB-538] - New OpenEngSB logo
    * [OPENENGSB-589] - document release process for stable branches

#### Sub-task
    * [OPENENGSB-83] - Implement the ui for doing method calls to the OpenEngSB connectors.
    * [OPENENGSB-85] - Retrieve and show domains and connectors in the UI
    * [OPENENGSB-86] - Show and manage instantiated connectors
    * [OPENENGSB-89] - Port simplest possible editor using the new configuration to instantiate an OpenEngSB connector. No attribute types, no validation.
    * [OPENENGSB-109] - Eventmodel
    * [OPENENGSB-110] - Event send view
    * [OPENENGSB-111] - Port drools engine
    * [OPENENGSB-112] - Sample Rule
    * [OPENENGSB-113] - Domainhelper porten
    * [OPENENGSB-114] - Document use case on hp
    * [OPENENGSB-117] - Domains implement domain interface themselves
    * [OPENENGSB-118] - Dummy context service implementation with query and mangement serive to put and retrieve domain-connector connection
    * [OPENENGSB-119] - Context set ui
    * [OPENENGSB-120] - Document on hp
    * [OPENENGSB-123] - event-send-ui: add feedback message if event has been sent
    * [OPENENGSB-124] - add form reset button
    * [OPENENGSB-125] - Remove EDB and corresponding util libaries
    * [OPENENGSB-128] - Define Domain Interface
    * [OPENENGSB-129] - Implement domain provider
    * [OPENENGSB-130] - Mail connector servicemanager interface & domain interface
    * [OPENENGSB-135] - Document on hp
    * [OPENENGSB-153] - create UI-component (frontend)
    * [OPENENGSB-154] - Update usecase on HP
    * [OPENENGSB-155] - show all existing rules in Event-UI
    * [OPENENGSB-156] - modify rules from rule-view in event-UI
    * [OPENENGSB-157] - hide setBundleContext in method-choice
    * [OPENENGSB-158] - display feedback after method-call
    * [OPENENGSB-159] - create services from Testclient-ui
    * [OPENENGSB-160] - Service-editor should support password fields (e.g. email)
    * [OPENENGSB-161] - support boolean datatypes (use checkbox) for service-properties
    * [OPENENGSB-162] - get rid of SerializationExceptions in wicketui
    * [OPENENGSB-163] - modify domain to support subject-prefixes
    * [OPENENGSB-164] - modify usecase-doc on HP
    * [OPENENGSB-165] - add Dropdownbox for selecting default-connector
    * [OPENENGSB-166] - display context-id in context-editor
    * [OPENENGSB-168] - remove package packets
    * [OPENENGSB-169] - Pax Composite file for adding karaf, debug and release settings
    * [OPENENGSB-170] - feature file for adding different connectors and openengsb parts
    * [OPENENGSB-361] - Create a UseTest for running flows
    * [OPENENGSB-363] - Flows should be able to "pause" in order to wait for new Events.
    * [OPENENGSB-364] - processEvent should propagate events to all running flows in the event's context

