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

