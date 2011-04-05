openengsb-1.2.0.M4 2011-03-23
--------------------------------------------

This milestone release contains various improvements and bugfixes. In addition the features packaing and the distribution has been completely refactored. In addition two new domains had been added and the exception model had been adapted.

### Highlights
  * Upgrade various libs to their latest versions
  * Provide only one distirbution for windows and linux
  * Split features into a useful and sane package size
  * Add Appointment and Contact domain
  * Make all exceptions untagged
  * Increase jdoc and stability at various places

### Details

** Bug
    * [OPENENGSB-1078] - Required bundle in drools metainf suppress features:uninstall/install
    * [OPENENGSB-1099] - jira connector - generating report is returning the type-code not the name
    * [OPENENGSB-1102] - Make scripts in etc/scripts executable
    * [OPENENGSB-1108] - Spring-security uses Threadlocal context-strategy in production environment (should be InheritedThreadLocal)
    * [OPENENGSB-1110] - workflow-service does not start flows in correct context
    * [OPENENGSB-1119] - Eclipse Checkstyle configuration has a bug
    * [OPENENGSB-1120] - Subproject page on hp looks wired
    * [OPENENGSB-1131] - archetype domain create invalid blueprint xml file
    * [OPENENGSB-1139] - Integration tests fail
    * [OPENENGSB-1146] - GitServiceImplTest.exportRepository_shouldReturnZipFileWithRepoEntries fails under Windows.
    * [OPENENGSB-1163] - Integration Tests time out (after 300000ms)
    * [OPENENGSB-1164] - Wiring integration tests fails

** Improvement
    * [OPENENGSB-676] - openengsb writes to ${karaf.data}/data/openengsb instead of ${karaf.data}/openengsb
    * [OPENENGSB-1041] - Add openticket logo to openengsb childproject page
    * [OPENENGSB-1042] - Add yaste logo to openengsb childproject page
    * [OPENENGSB-1052] - use String[] to manage loctions in service-properties
    * [OPENENGSB-1079] - Cleanup and split features.xml
    * [OPENENGSB-1104] - Make Events removable from WorkflowEditor
    * [OPENENGSB-1111] - provide abstract classes or util-methods to deal with Context in threadpools
    * [OPENENGSB-1115] - Make JMSTemplateFactory thread safe
    * [OPENENGSB-1118] - Create checkstyle script
    * [OPENENGSB-1123] - Merge win and linux distribution
    * [OPENENGSB-1125] - Enhance JDoc for ports (remote communciation)
    * [OPENENGSB-1126] - Remove IncomingPort interface
    * [OPENENGSB-1127] - Transform OsgiServiceNotAvailableException into a runtime exception
    * [OPENENGSB-1147] - use environment variable to enable debug-output

** Library Upgrade
    * [OPENENGSB-1092] - Extract com.atlassian.jira.plugins 4.1.1
    * [OPENENGSB-1150] - upgrade to openengsb-maven-plugin 1.3.3 stable
    * [OPENENGSB-1156] - Upgrad maven-assembly-plugin to 2.2.1
    * [OPENENGSB-1157] - Upgrade maven-resources-plugin to 2.5
    * [OPENENGSB-1158] - Upgrade maven-surefire-plugin to 2.8
    * [OPENENGSB-1159] - Upgrade maven-surefire-report-plugin to 2.8
    * [OPENENGSB-1160] - Upgrade to openengsb-root-13

** New Feature
    * [OPENENGSB-1006] - Add contact domain for OpenEngSB
    * [OPENENGSB-1007] - Add appointment domain to openengsb
    * [OPENENGSB-1124] - Add RemoteCommunicationException and OsgiServiceException to ports-communication-implementation


** Task
    * [OPENENGSB-983] - release openengsb-1.2.0.M4
    * [OPENENGSB-984] - Define release name using O O
    * [OPENENGSB-1105] - Remove old licenses from WorkflowEditor html files
    * [OPENENGSB-1140] - Add Felix Mayerhuber as contributor
    * [OPENENGSB-1151] - remove no longer required update-version-info.sh script

openengsb-1.2.0.M3 2011-03-09
--------------------------------------------

This milestone release contains many improvements in the manual and on the hompage and an upgrade and some upgrades to some core concepts. In addition many libraries where upgraded. You should really scan the detailed list to find if and mostly where this release affects you.

### Highlights
  * Improved manual in various places
  * Improved homepage at various places 
  * Karaf 2.2.0
  * OpenEngSB pages are now mounted in better names
  * Updated C# examples
  * Updated SCM Domain
  * Changed to a more dynamic wiriing approach
  * Added a root-conext
  * Replace Domain-services by DomainEndpointFactory

### Details

** Bug
    * [OPENENGSB-971] - Download page linux and windows link both point to windows distributions
    * [OPENENGSB-972] - Integration tests no longer run with karaf-2.2.0
    * [OPENENGSB-994] - Typo: Website Download
    * [OPENENGSB-1004] - Show icons side-by-side with text on openengsb.org
    * [OPENENGSB-1016] - background looks odd when displaying manual
    * [OPENENGSB-1027] - Quickstart example points to src releases, not binary releases
    * [OPENENGSB-1031] - missing </para> tags in documentation
    * [OPENENGSB-1034] - Export Worfklowservice with correct interfaces
    * [OPENENGSB-1039] - Proxy bundle.properties does not contain all properties
    * [OPENENGSB-1043] - Example at openengsb.org does not include that it has to be installed first
    * [OPENENGSB-1045] - JMS Ports implementation does not use dynamic import
    * [OPENENGSB-1046] - Workflow service is not exported with correct interfaces
    * [OPENENGSB-1076] - Rendering problem on HP using internet explorer
    * [OPENENGSB-1077] - openengsb.org not reachable by MS Internet Explorer

** Epic
    * [OPENENGSB-844] - First Prototype of Web-based Workflow Editor

** Improvement
    * [OPENENGSB-615] - Mount openengsb wicket pages to better names
    * [OPENENGSB-650] - Proivde xsd file for feature.xml for karaf community
    * [OPENENGSB-734] - scm domain interface 2
    * [OPENENGSB-797] - Homepage Design: Black Header
    * [OPENENGSB-816] - Using karaf obr feature resolver
    * [OPENENGSB-817] - Use karafs new standard and enterprise feature file
    * [OPENENGSB-818] - Use karafs improved feature file assembly capabilities
    * [OPENENGSB-839] - Use karafs features definition instead of own one
    * [OPENENGSB-854] - Eye catcher - Graphical overview
    * [OPENENGSB-857] - Explain usefulness of OpenEngSB on frontpage
    * [OPENENGSB-859] - Layout - Smoothen OpenEngSB logo on Frontpage
    * [OPENENGSB-860] - Online manual usability
    * [OPENENGSB-862] - Layout - Update OpenEngSB logo on Facebook
    * [OPENENGSB-864] - Spellcheck user manual
    * [OPENENGSB-899] - Replace bundleContext.getServiceReference usage by using ServiceTracker
    * [OPENENGSB-913] - Workflow service should retrieve connector-instances at runtime
    * [OPENENGSB-961] - Use URL-params to determine a the current contextID rather than a session-variable
    * [OPENENGSB-962] - make service-ranking configurable in connector-config
    * [OPENENGSB-963] - extends service-utils to support "locations"
    * [OPENENGSB-988] - provide functionality to cancel workflows
    * [OPENENGSB-1000] - Add source download option to download page
    * [OPENENGSB-1018] - Document proxy creation
    * [OPENENGSB-1026] - Provide example and documentation for executing Workflows remotely
    * [OPENENGSB-1029] - Remove subprojects part from menu to single page
    * [OPENENGSB-1030] - Remove occurence of ContextStore Completely from documentation
    * [OPENENGSB-1040] - Add opencit logo to openengsb childproject page
    * [OPENENGSB-1044] - Use github/jira id for commiter on team page
    * [OPENENGSB-1047] - Improve serialization capabilities of processbags
    * [OPENENGSB-1051] - Reorder bundles in features.xml
    * [OPENENGSB-1053] - Group examples rather according to language then topic
    * [OPENENGSB-1071] - Add opencit logo to openengsb childprojects page, again.
    * [OPENENGSB-1072] - refactor subprojects page to use linkbox layout
    * [OPENENGSB-1080] - Remove "Release Candidate" from link list
    * [OPENENGSB-1088] - Set development version also manual in root pom

** Infrastructure
    * [OPENENGSB-989] - Get signs for contributor license

** Library Upgrade
    * [OPENENGSB-992] - upgrade wicket to 1.4.16
    * [OPENENGSB-1002] - Upgrade karaf to 2.2.0
    * [OPENENGSB-1014] - Upgrade to pax-exam 1.2.4
    * [OPENENGSB-1015] - upgrade guava to r08
    * [OPENENGSB-1061] - Upgrade to openengsb-maven-plugin-1.3.0
    * [OPENENGSB-1062] - Upgrade to openengsb-root-10
    * [OPENENGSB-1091] - Extract axis-all 1.3 as own projects

** New Feature
    * [OPENENGSB-472] - Issue domain has to support additional functions
    * [OPENENGSB-506] - include checkstyle in openengsb maven plugin
    * [OPENENGSB-729] - Allow some kind of "default" context container always there
    * [OPENENGSB-775] - Allow multiple connectors of one domain
    * [OPENENGSB-822] - More dynamic wiring approach
    * [OPENENGSB-908] - export location properties for a specific connector into osgi
    * [OPENENGSB-909] - Load connector instances from osgi via location properties
    * [OPENENGSB-974] - Root-Context
    * [OPENENGSB-986] - Service manager should be able to set service ranking
    * [OPENENGSB-1009] - Replace Domain-services by DomainEndpointFactory
    * [OPENENGSB-1012] - prePush mojo
    * [OPENENGSB-1025] - WorkflowService should return Workflow result after execution
    * [OPENENGSB-1048] - Add new annotation to ignore properties in neodatis (persistence backend)

** Task
    * [OPENENGSB-293] - Update C# example
    * [OPENENGSB-867] - update etc/scripts/* to use the openegsb-maven-plugin
    * [OPENENGSB-940] - Define release name using N N
    * [OPENENGSB-941] - release openengsb-1.2.0.M3
    * [OPENENGSB-990] - change license in all files to "vereinslicense"
    * [OPENENGSB-996] - handle all connector-persistence via .connector-files
    * [OPENENGSB-1001] - Document how to add local pre-push scripts to gitrepo
    * [OPENENGSB-1019] - clean release profiles
    * [OPENENGSB-1022] - Add Christoph Karner as commiter
    * [OPENENGSB-1023] - Add Roland Bair as contributor
    * [OPENENGSB-1024] - Add thomas moser as contributor
    * [OPENENGSB-1038] - Move maven notice plugin version to openengsb-root
    * [OPENENGSB-1056] - change license in all files to "vereinslicense"
    * [OPENENGSB-1063] - Upgrade copyright in manual to 2009-2011
    * [OPENENGSB-1066] - Add openengsb-maven-plugin to openengsb-root
    * [OPENENGSB-1067] - errors in the license
    * [OPENENGSB-1075] - move core/ports to ports
    * [OPENENGSB-1081] - Upgrade <openengsb.version.stable> to 1.1.0.RELEASE
    * [OPENENGSB-1093] - Upgrade openengsb-wrapped-bundle to openengsb-root-10


openengsb-1.2.0.M2 2011-02-23
--------------------------------------------

Alhough a milestone release it only contains small changes with an upgrade to karaf-2.1.4, upgrade to equinox instead of felix and some minor enhancements and bugfixes.

### Highlights
  * Use Equinox instead of Felix
  * Karaf 2.1.4
  * SSH support for git connector

### Details
** Bug
    * [OPENENGSB-872] - refresh page OverviewPanel for Tasks
    * [OPENENGSB-873] - TaskId mapping
    * [OPENENGSB-881] - maven-connector throws *StartEvent too early
    * [OPENENGSB-944] - null-task is not rendered correctly
    * [OPENENGSB-946] - Latest felix-fileinstall version only in karaf-2.2; therefore filedeployer fails to start
    * [OPENENGSB-949] - AbstractOpenEngSBInvocationHandler does not work correctly on proxies
    * [OPENENGSB-950] - KNOWN_ISSUES and CHANGELOG duplicated in windows distribution
    * [OPENENGSB-952] - Git connector uses the wrong domain
    * [OPENENGSB-953] - Empty ServiceId names are not editable
    * [OPENENGSB-966] - failing unit-test for jgit
    * [OPENENGSB-977] - Internal config listener conflicts with karaf

** Improvement
    * [OPENENGSB-915] - git-connector should support ssh-key-authentication
    * [OPENENGSB-945] - lower log-level for error during annotation retrieving
    * [OPENENGSB-955] - Use equinox instead of felix
    * [OPENENGSB-958] - Maximum number of logfiles for maven-connector

** Library Upgrade
    * [OPENENGSB-957] - Upgrade karaf 2.1.3 to 2.1.4
    * [OPENENGSB-982] - Upgrade to openengsb-root-8

** New Feature
    * [OPENENGSB-725] - Add filters to directly retrieve specific connectors
    * [OPENENGSB-954] - Report-domain should support "list"-method

** Task
    * [OPENENGSB-378] - reevaluate if jgit api supports high level calls
    * [OPENENGSB-433] - automatically create notice and license parts/files
    * [OPENENGSB-901] - Define release name using M M
    * [OPENENGSB-902] - release openengsb-1.2.0.M2
    * [OPENENGSB-936] - remove unnecessary openengsb-maven-plugin version from license-check profile in poms/pom
    * [OPENENGSB-956] - Set the connector deployment service listening on the etc folder and the config folder
    * [OPENENGSB-959] - move ContextIdFilter to ui-common
    * [OPENENGSB-970] - Add Stefan Paula as contributor
    * [OPENENGSB-975] - Identify PAX as optional in Contribution how-to


openengsb-1.2.0.M1 2011-02-09
--------------------------------------------

Besides many bugfixes and minor improvements this release contains 9 dependency upgrades, the extraction of the openengsb-maven-plugin and the possibility to create connectors from configuration files.

### Highlights
  * Added connector deployer
  * Move openengsb-maven-plugin to own subproject and change namespace to org.openengsb
  * SpringOsgiBundle Wicket Inject had been removed (we use blueprint instead now)
  * Upgrade archetypes to use blueprint instead of spring-dm
  * Added system-user to register and use internal connectors.
  * Authorities could be granted now via annotations
  * UI Tests could be localized

### Details
** Bug
    * [OPENENGSB-680] - data backward compatibility stored in neodatis backend
    * [OPENENGSB-837] - Strange HUGE spaces between headings in generated html from docbook
    * [OPENENGSB-852] - class not visible from class loader (even though it's a package internal access)
    * [OPENENGSB-883] - test for pushVersion mojo fails on windows
    * [OPENENGSB-886] - openengsb-port-jms tests are failing
    * [OPENENGSB-919] - source README.md is outdated
    * [OPENENGSB-921] - Move OpenEngSB references from poms/pom.xml to root pom

** Improvement
    * [OPENENGSB-611] - rule-editor should use indicator while saving rules etc.
    * [OPENENGSB-614] - Merge code specific artifacts with code
    * [OPENENGSB-744] - Create "system"-user that connectors use to execute root-tasks (setup)
    * [OPENENGSB-780] - make names in openengsb/ui wicket specific
    * [OPENENGSB-784] - Pages loading pages potentially JMS have to load via ajax
    * [OPENENGSB-808] - Add helper methods to retrieve servicemanagers in code
    * [OPENENGSB-831] - usability fixes for Task and TaskboxService
    * [OPENENGSB-853] - write maven-connector's maven-output to logfile
    * [OPENENGSB-874] - create "system-user" for internal use in connectors
    * [OPENENGSB-884] - allow granting Authorities in service-methods through annotations
    * [OPENENGSB-892] - Adapt namespace of openengsb-maven-plugin to org.openengsb.tooling.openengsb-maven-plugin
    * [OPENENGSB-904] - Create quickrun script
    * [OPENENGSB-905] - make UI Tests localizable

** Library Upgrade
    * [OPENENGSB-875] - upgrade commons-io to 2.0.1
    * [OPENENGSB-887] - Upgrade maven-plugin-plugin to 2.6
    * [OPENENGSB-888] - Upgrade maven-shade-plugin to 1.4
    * [OPENENGSB-889] - Upgrade maven-surefire-plugin to 2.7.2
    * [OPENENGSB-890] - Upgrade maven-surefire-report-plugin to 2.7.2
    * [OPENENGSB-914] - add karaf features-maven-plugin to openengsb-root
    * [OPENENGSB-922] - Upgrade to maven-bundle-plugin 2.3.4
    * [OPENENGSB-924] - Upgrade felix-fileinstall to 3.1.10
    * [OPENENGSB-933] - Upgrade to openengsb-maven-plugin 1.2.0

** New Feature
    * [OPENENGSB-727] - Create connector instances from a config file
    * [OPENENGSB-825] - Move openengsb-maven-plugin to own subproject
    * [OPENENGSB-893] - enhance usermanagement-interface to assign roles to users

** Task
    * [OPENENGSB-597] - document test naming schema and coding style
    * [OPENENGSB-800] - port archetypes to use blueprint instead of spring-dm
    * [OPENENGSB-813] - Define release name using L L
    * [OPENENGSB-855] - Presentation link broken on frontpage
    * [OPENENGSB-856] - "Additional Material" PDFs broken on Adobe Reader X
    * [OPENENGSB-858] - Layout - set google user group color to red
    * [OPENENGSB-877] - Provide release notes for openengsb-1.2.0.M1
    * [OPENENGSB-882] - improve log-output in email-connector
    * [OPENENGSB-891] - Move repositories from openengsb/pom.xml into openengsb-root
    * [OPENENGSB-895] - update openengsb-maven-plugin section in user manual
    * [OPENENGSB-923] - Move openengsb-maven-plugin to org.openengsb namespace
    * [OPENENGSB-926] - Remove springbundle-wicket inject

** TBD
    * [OPENENGSB-653] - evaluate JAAS in the context of the openengsb
