openengsb-1.1.3.RELEASE 2011-04-22
-------------------------------------------------------

This release contains various bug fixes, fixed of potential bugs, and improvements mostly to the documentation and the homepage.

### Highlights
  * Increase documentation density
  * Stabilze (human)task-functionallity
  * Various workflow improvements

### Details
** Bug
    * [OPENENGSB-1049] - Testclient does no longer allow to call via domains
    * [OPENENGSB-1122] - updateService in AbstractServiceManager allows inconsistent IDs
    * [OPENENGSB-1134] - Exception when starting ssh-server
    * [OPENENGSB-1138] - bundle-properties in core-common not localized properly
    * [OPENENGSB-1146] - GitServiceImplTest.exportRepository_shouldReturnZipFileWithRepoEntries fails under Windows.
    * [OPENENGSB-1170] - archetype connector create invalid blueprint xml file
    * [OPENENGSB-1195] - Connector archetype create ServiceManager with wrong constructor
    * [OPENENGSB-1207] - itests print DEBUG-output
    * [OPENENGSB-1218] - testclient throws for every service which is called a nullpointer exception
    * [OPENENGSB-1230] - notification of rule persistence failure
    * [OPENENGSB-1231] - WorkflowService has to extend OpenEngSBService

** Improvement
    * [OPENENGSB-1070] - cancel workflow and all associated (open) tasks
    * [OPENENGSB-1147] - use environment variable to enable debug-output
    * [OPENENGSB-1148] - waitForFlowToFinish should indicate if the flow is finished.
    * [OPENENGSB-1233] - ability to remove rules from the rule base
    * [OPENENGSB-1265] - Move HowTo's to DocBook
    * [OPENENGSB-1267] - Client & Shell Script Missing in "Pink Panther"
    * [OPENENGSB-1318] - Prevent non-required karaf default features from startup

** Task
    * [OPENENGSB-1133] - remove Feedback-panels from all custom panels
    * [OPENENGSB-1145] - Release openengsb-1.1.3.RELEASE
    * [OPENENGSB-1151] - remove no longer required update-version-info.sh script
    * [OPENENGSB-1336] - Upgrade openengsb dev version to openengsb-1.2.0.M5

openengsb-1.1.2.RELEASE 2011-03-22
-------------------------------------------------------

This release contains various bug fixes, fixed of potential bugs, and improvements mostly to the documentation and the homepage.

### Highlights
  * .zip distribution contains windows and linux files now
  * Workflows can be cancled now
  * All tmp data is stored in  ${karaf.data}/openengsb again

### Details
** Bug
    * [OPENENGSB-1120] - Subproject page on hp looks wired
    * [OPENENGSB-1131] - archetype domain create invalid blueprint xml file
    * [OPENENGSB-1136] - SerializeException in OpenEngSBPage

** Improvement
    * [OPENENGSB-676] - openengsb writes to ${karaf.data}/data/openengsb instead of ${karaf.data}/openengsb
    * [OPENENGSB-988] - provide functionality to cancel workflows
    * [OPENENGSB-1041] - Add openticket logo to openengsb childproject page
    * [OPENENGSB-1042] - Add yaste logo to openengsb childproject page
    * [OPENENGSB-1123] - Merge win and linux distribution

** Task
    * [OPENENGSB-1117] - Release openengsb-1.1.2.RELEASE

openengsb-1.1.1.RELEASE 2011-03-11
-------------------------------------------------------

This release contains various bug fixes, fixed of potential bugs, and improvements mostly to the documentation and the homepage

### Highlights
  * Homepage usable again from internet explorer

### Details
** Bug
    * [OPENENGSB-1076] - Rendering problem on HP using internet explorer
    * [OPENENGSB-1077] - openengsb.org not reachable by MS Internet Explorer
    * [OPENENGSB-1106] - AbstractOpenEngSBInvocationHandler cannot invoke method called "notify"
    * [OPENENGSB-1108] - Spring-security uses Threadlocal context-strategy in production environment (should be InheritedThreadLocal)
    * [OPENENGSB-1110] - workflow-service does not start flows in correct context

** Improvement
    * [OPENENGSB-961] - Use URL-params to determine a the current contextID rather than a session-variable
    * [OPENENGSB-1040] - Add opencit logo to openengsb childproject page
    * [OPENENGSB-1071] - Add opencit logo to openengsb childprojects page, again.
    * [OPENENGSB-1072] - refactor subprojects page to use linkbox layout
    * [OPENENGSB-1080] - Remove "Release Candidate" from link list
    * [OPENENGSB-1088] - Set development version also manual in root pom
    * [OPENENGSB-1115] - Make JMSTemplateFactory thread safe

** Task
    * [OPENENGSB-1001] - Document how to add local pre-push scripts to gitrepo
    * [OPENENGSB-1068] - Release openengsb-1.1.1.RELEASE

openengsb-1.1.0.RELEASE 2011-03-08
-------------------------------------------------------

Finally the openengsb-1.1.x branch reached a stability to release an 1.1.0 final release. The OpenEngSB
Team is proud to bring to you the next major release of the OpenEngSB containing hundreds of new features,
improvements and bugfixes. There are definitely too many changes to name them in the short space here,
but you can upgrade from openengsb-1.0.0 by simply follow the instructions for the single releases. Our
absolute highlights for this release are

### Highlights
  * Human Tasks
  * Highly improved documentation and homepage
  * External openengsb-maven-plugin handling all parts formerly handled by scripts
  * Three new connectors and domains
  * Enhanced security, workflow and domain model
  * Karaf-2.2 feature model
  * No warnings and errors printed after the startup of the OpenEngSB
  * Scripts to run the OpenEngSB as service by default

openengsb-1.1.0.RC3 2011-03-06
-----------------------------------------------------

This release contains further stabilisations toward openengsb-1.1.0.RELEASE

### Highlights
  * Corrected Homepage Design
  * Increase Documentation
  * Fix various bugs

### Details
** Bug
    * [OPENENGSB-971] - Download page linux and windows link both point to windows distributions
    * [OPENENGSB-994] - Typo: Website Download
    * [OPENENGSB-1004] - Show icons side-by-side with text on openengsb.org
    * [OPENENGSB-1016] - background looks odd when displaying manual
    * [OPENENGSB-1027] - Quickstart example points to src releases, not binary releases
    * [OPENENGSB-1034] - Export Worfklowservice with correct interfaces
    * [OPENENGSB-1043] - Example at openengsb.org does not include that it has to be installed first
    * [OPENENGSB-1045] - JMS Ports implementation does not use dynamic import
    * [OPENENGSB-1046] - Workflow service is not exported with correct interfaces

** Improvement
    * [OPENENGSB-797] - Homepage Design: Black Header
    * [OPENENGSB-854] - Eye catcher - Graphical overview
    * [OPENENGSB-857] - Explain usefulness of OpenEngSB on frontpage
    * [OPENENGSB-859] - Layout - Smoothen OpenEngSB logo on Frontpage
    * [OPENENGSB-860] - Online manual usability
    * [OPENENGSB-862] - Layout - Update OpenEngSB logo on Facebook
    * [OPENENGSB-864] - Spellcheck user manual
    * [OPENENGSB-905] - make UI Tests localizable
    * [OPENENGSB-1000] - Add source download option to download page
    * [OPENENGSB-1018] - Document proxy creation
    * [OPENENGSB-1029] - Remove subprojects part from menu to single page

** Library Upgrade
    * [OPENENGSB-982] - Upgrade to openengsb-root-8
    * [OPENENGSB-992] - upgrade wicket to 1.4.16

** Task
    * [OPENENGSB-968] - release openengsb-1.1.0.RC3
    * [OPENENGSB-970] - Add Stefan Paula as contributor
    * [OPENENGSB-975] - Identify PAX as optional in Contribution how-to
    * [OPENENGSB-1063] - Upgrade copyright in manual to 2009-2011

openengsb-1.1.0.RC2 2011-02-18
-----------------------------------------------------

This release contains further stabilisations toward openengsb-1.1.0.RELEASE

### Highlights
  * Also using dedicated openengsb-maven-plugin
  * Karaf 2.1.4
  * Many fixes and minor improvements in various connectors

### Details
** Bug
    * [OPENENGSB-680] - data backward compatibility stored in neodatis backend
    * [OPENENGSB-837] - Strange HUGE spaces between headings in generated html from docbook
    * [OPENENGSB-872] - refresh page OverviewPanel for Tasks
    * [OPENENGSB-881] - maven-connector throws *StartEvent too early
    * [OPENENGSB-944] - null-task is not rendered correctly
    * [OPENENGSB-949] - AbstractOpenEngSBInvocationHandler does not work correctly on proxies
    * [OPENENGSB-950] - KNOWN_ISSUES and CHANGELOG duplicated in windows distribution
    * [OPENENGSB-952] - Git connector uses the wrong domain
    * [OPENENGSB-953] - Empty ServiceId names are not editable
    * [OPENENGSB-966] - failing unit-test for jgit

** Improvement
    * [OPENENGSB-808] - Add helper methods to retrieve servicemanagers in code
    * [OPENENGSB-915] - git-connector should support ssh-key-authentication
    * [OPENENGSB-945] - lower log-level for error during annotation retrieving
    * [OPENENGSB-955] - Use equinox instead of felix
    * [OPENENGSB-958] - Maximum number of logfiles for maven-connector
    * [OPENENGSB-969] - Also use dedicated openengsb-maven-plugin

** Library Upgrade
    * [OPENENGSB-933] - Upgrade to openengsb-maven-plugin 1.2.0
    * [OPENENGSB-957] - Upgrade karaf 2.1.3 to 2.1.4

** New Feature
    * [OPENENGSB-954] - Report-domain should support "list"-method

** Task
    * [OPENENGSB-433] - automatically create notice and license parts/files
    * [OPENENGSB-597] - document test naming schema and coding style
    * [OPENENGSB-800] - port archetypes to use blueprint instead of spring-dm
    * [OPENENGSB-900] - release openengsb-1.1.0.RC2
    * [OPENENGSB-923] - Move openengsb-maven-plugin to org.openengsb namespace
    * [OPENENGSB-936] - remove unnecessary openengsb-maven-plugin version from license-check profile in poms/pom
    * [OPENENGSB-959] - move ContextIdFilter to ui-common

openengsb-1.1.0.RC1 2011-02-01
-----------------------------------------------------

Changes by now...

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
  * allow distribution via different OpenEngSBs
  * Taskbox support for human flows
  * OpenEngSB Maven plugin tool suite via openengsb-maven-plugin
  * openengsb-maven-plugin mojo for license plugin
  * openengsb-maven-plugin mojo for push-version
  * openengsb-maven-plugin mojo for assembly-mojo
  * More util methods for registering and using openengsb core services
  * Upgrade of jgit, maven-bundle-plugin
  * New internal system user for connector/domain setup
  * Granting authorities via annotations
  * Maven connector writes seperate log file
  * Usermanagement handles roles now

