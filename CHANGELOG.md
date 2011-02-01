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

### Details

