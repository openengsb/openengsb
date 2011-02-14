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
