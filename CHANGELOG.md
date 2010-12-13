openengsb-1.1.0.RELEASE
-----------------------

### Bug fixes
  * [e.g.] Fix problem with setting files

### New Projects
  * maven-openengsb-plugin
  * openengsb-core-taskbox
  * openengsb-core-usermanagement
  * openengsb-ui-taskbox
  * openengsb-ui-common-wicket
  * openengsb-connector-jira

### Removed Projects

### Upgraded Projects
 * org.apache.wicket/wicket-*/1.4.13
 * org.neodatis.odb/neodatis-odb/1.9.30.687
 * junit/junit/4.8.2
 * twdata.cli.version/1.0.5
 * org.springframework.security/spring-security-*/3.0.5.RELEASE
 * org.apache.maven.plugins/maven-surefire-plugin/2.6
 * org.apache.maven.plugins/maven-surefire-report-plugin/2.6
 * org.apache.maven.plugins/maven-compiler-plugin/2.3.2
 * org.apache.maven.plugins/maven-clean-plugin/2.4.1
 * org.apache.maven.plugins/maven-jar-plugin/2.3.1
 * org.apache.maven.plugins/maven-install-plugin/2.3.1
 * org.apache.maven.plugins/maven-resources-plugin/2.4.3
 * org.apache.maven.plugins/maven-checkstyle-plugin/2.6
 * apache-commons/commons-lang/2.6

### Remvoed External References

### New Features & Changed Behaviour
  * Added tooling plugin to execute license-check plugin and assemble scripts via mvn.

### Depricated or Removed Features

openengsb-1.0.2.RELEASE
-----------------------

### Bug fixes
  * fix a problem when building the project in a path that contains spaces
  * fix failing maven-connector-test on consecutive "install" calls without prior "clean"

### New Features & Changed Behaviour
  * workflow-service: remove droolsHelper and use drools-provided methods for starting workflows in rule-consequences (used in registerEventForFlow)
  * connector-email: automatically add a space after the prefix.
  * restructure Event-Hierarchy: xEndEvent -> xSuccessEvent and xFailEvent (for deploy, build and test)
  * add "processId"-field to event. Events where this property is set are only signaled to this process instead of all of them (they are still inserted into the workingmemory for the global rules).
  * Domains build, test, and deploy now provide additional methods that take a processId as argument. All events that are raised in these methods are only signaled to this processId.
  * Plaintext-report now uses getter methods of Events rather than direct reflection on the fields for report generation.
  * improve logging in workflow engine
  * when starting a flow, the ProcessInstance is now inserted into the workingmemory for use inside the workflow.
  * startFlow now triggers a "FlowStartedEvent" as soon as the processInstance has been inserted.

openengsb-1.0.1.RELEASE
-----------------------

### Bug fixes
  * wrapped: fix bug in wrapped wicket, that caused the back-button to break the UI.

### New Features & Changed Behaviour
  * workflow-service: retract events from working-memory when all rules have fired.

openengsb-1.0.0.RELEASE
-----------------------

Initial release

