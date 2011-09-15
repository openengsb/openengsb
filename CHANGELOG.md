openengsb-framework-1.2.2.RELEASE 2011-09-15
--------------------------------------------

This release of OpenEngSB is based off of the 1.2.x series branch, representing an update to OpenEngSB Framework 1.2.0.RELEASE. In sum 38
issues had been fixed. Among them it contains bug fixes identified in the prior release, and introduces improvements including; It includes
the introduction of paxexam fixing all integration test problems. In addition it is possible now to delete the context and show additional
information in the karaf shell. The homepage and documentation had been enhanced at various places. Finally there had been upgrades to
karaf and openengsb-root and pax-exam.

** Bug
    * [OPENENGSB-1570] - taskboxService.getTaskPanel throws wicket rendering error, when used in subproject at version: 1.2.0.RC1
    * [OPENENGSB-1680] - Superclass setup-method is not called before subclass setup-methods
    * [OPENENGSB-1848] - Karaf seems to start fine, but doesnt load any bundles
    * [OPENENGSB-1859] - Connecotr & Domain links in documentation do not work
    * [OPENENGSB-1868] - Unused interface ContextConnectorService
    * [OPENENGSB-1875] - Icons are not shown correctly
    * [OPENENGSB-1879] - Choosing WiringPage auditing connector on wiring page throws ClassCastException
    * [OPENENGSB-1881] - Update service page throws a WicketNotSerializableException:
    * [OPENENGSB-1911] - Possible concurrent modification exception in DummyPersistence
    * [OPENENGSB-1912] - Clash between lib and framework exporting both xml.stream
    * [OPENENGSB-1918] - Remove developer and contributor from openengsb-root to openengsb
    * [OPENENGSB-1928] - license-mapping.xml lists antlr as Apache licensed
    * [OPENENGSB-1949] - Referencing openengsb-core in port features fails terrible at installing them one after the other
    * [OPENENGSB-1950] - OpenEngSB csharp examples are out-of-sync with master
    * [OPENENGSB-1952] - Refreshing openengsb-core does not work
    * [OPENENGSB-1960] - Not a directory bug thrown in ContextFilePersistenceService
    * [OPENENGSB-1974] - openengsb-1.2.x branch points to nonexistent features/connector/maven commit

** Improvement
    * [OPENENGSB-1358] - Document features.xml
    * [OPENENGSB-1571] - Increase default memory used during windows bootup
    * [OPENENGSB-1916] - Add description and details section to features.xml
    * [OPENENGSB-1921] - Use same mail dependency as karaf
    * [OPENENGSB-1929] - OpenEngSB window shows karaf instead of OpenEngSB

** Library Upgrade
    * [OPENENGSB-1235] - Upgrade to pax-exam 2.3.0.M1
    * [OPENENGSB-1926] - Upgrade to openengsb-root-20
    * [OPENENGSB-1946] - Upgrade to karaf-2.2.3
    * [OPENENGSB-1979] - Upgrade to paxexam-karaf-0.1.0

** New Feature
    * [OPENENGSB-1795] - add method to delete context
    * [OPENENGSB-1853] - Show openengsb system information if info is issued in karaf shell

** Task
    * [OPENENGSB-1693] - add an explanation for every bundle which uses Dynamic Import
    * [OPENENGSB-1757] - Release framework-1.2.2
    * [OPENENGSB-1915] - Replace tabs with spaces in features.xml
    * [OPENENGSB-1930] - Define TestResources for wicket projects (admin & ui common)
    * [OPENENGSB-1941] - Add intellij idea to sponsers page
    * [OPENENGSB-1944] - Update Felix as PMC
    * [OPENENGSB-1951] - Check if tcp://xyz:asf?blub also works in json-jms definitions
    * [OPENENGSB-1961] - Add oxygenxml to sponsors page
    * [OPENENGSB-1968] - Use org.openengsb.labs.paxexam.karaf for integration testing
    * [OPENENGSB-1970] - correct scm-property in all framework-poms


openengsb-framework-1.2.1.RELEASE 2011-07-14
--------------------------------------------

This release of Apache Karaf is based off of the 2.2.x series branch, representing an update to Apache Karaf 1.2.0.RELEASE. It contains
bug fixes identified in the prior release, and introduces improvements including; pipeline infrastructure for remoting. completely 
adapted context store. make entire ports infrastructure internal and no longer visible for other bundles. increase stability of the
Admin UI. The entire OpenEngSB model has jaxb annotations now. Documentation enhancements at various places. There has also been a few
small dependency upgrades to karaf and openengsb-root.

** Bug
    * [OPENENGSB-1597] - karaf.data, karaf.base and karaf.home are not set in itests
    * [OPENENGSB-1638] - OpenEngSB uses org.apache.aries.transaction.manager in version 0.2-incubating
    * [OPENENGSB-1771] - Documentation still points at old hudson instance
    * [OPENENGSB-1772] - Documentation does not clearify that we do not want to use our own mvn repository
    * [OPENENGSB-1774] - Json-Marshalling: ClassNotFoundException
    * [OPENENGSB-1779] - Instance Id cannot be reused
    * [OPENENGSB-1810] - Creating two connectors with the same name throws an uncatched exception in the UI
    * [OPENENGSB-1816] - Messages via jms are always sent to RECEIVE

** Epic
    * [OPENENGSB-267] - adapt context store design and name

** Improvement
    * [OPENENGSB-1008] - Add external, interesting tutorials to hp and usermanual
    * [OPENENGSB-1187] - configure karaf-ports in tests to something different than opencit uses
    * [OPENENGSB-1285] - Exception after creating an proxy
    * [OPENENGSB-1326] - Extract entire common call-infrastructure from ports
    * [OPENENGSB-1327] - Make entire ports infrastructure internal
    * [OPENENGSB-1799] - Use script based solution instead of pax:provision
    * [OPENENGSB-1808] - Increase readability of org.apache.karaf.features.cfg
    * [OPENENGSB-1811] - ConnectorRegistrationManagerImpl#finishCreatingInstance does not handle case that factory returns null
    * [OPENENGSB-1812] - Cleanup code of (auto-generated) TODOs

** Library Upgrade
    * [OPENENGSB-1766] - Upgrade to openengsb-root-19
    * [OPENENGSB-1778] - Upgrade to karaf-2.2.2

** New Feature
    * [OPENENGSB-1482] - Refactor ports implementation to "pipeline"-infrastructure (to loosen the coupling)

** Task
    * [OPENENGSB-1417] - Redesign remote messaging from ground up
    * [OPENENGSB-1562] - add environment-variable to override the log-level in Tests
    * [OPENENGSB-1564] - put jaxb-annotations in models in core-api
    * [OPENENGSB-1614] - Add documentation for pipeline API
    * [OPENENGSB-1660] - Add templates for eclipse and intellij to etc/
    * [OPENENGSB-1670] - deprecate ContextCurrentService.{get,set}ThreadLocalContext methods
    * [OPENENGSB-1756] - Release openengsb-1.2.1.RELEASE
    * [OPENENGSB-1773] - Purpose and configuration of openengsb-maven-plugin missing in documentation
    * [OPENENGSB-1786] - Move workflow tests from itest project into workflow project


openengsb-framework-1.2.0.RELEASE 2011-06-22
--------------------------------------------

After five milestone and three RC releases the OpenEngSB team finally decides that openengsb-1.2.0.RELEASE is ready for
the public. This release is based on the 1.2.x branch. Compared to our last stable version (1.1.x) we packed lots of new
features, bug-fixes and improvements into this release. One of the most noticable features is that connectors could be
configured via configuration files now. OpenEngSB 1.2.0 runs on Apache Karaf 2.2.1. In addition a root context had been
added over the actual context approach. The Domain-services get replaced by DomainEndpointFactory reducing the required
code to write by far. All exmaple connectors (csharp, python, ...) had been updated to the latest version. Domains and
features had been extracted into own subprojects to increase the speed. To complete this step there is a new bundle
project now containing all domains and connectors at once. Another important new point in the switch from 1.1.x to 1.2.x
is that the old core-common project had been splitted into core-api, core-common and core-services allowing a cleaner
update and reference approach. Webservices are supported now via CXF 2.4.1 and JMS via AMQ 5.5. Both allow a blueprint
configuration of the endpoints. Enhancements and fixes where also added to the console, the admin-ui and the 
documentation. In addition we've done a complete rewrite of the ServiceManager allowing a much cleaner connector-domain
approach in 1.2.x. 

### Details
** New Feature
    * [OPENENGSB-1745] - Add simple script to show the possible tags in submodule folders

** Task
    * [OPENENGSB-1611] - Release openengsb-1.2.0.RELEASE
    * [OPENENGSB-1744] - Upgrade maven connector reference to openengsb-connector-maven-1.2.3


openengsb-framework-1.2.0.RC3 2011-06-18
--------------------------------------------

This release is the third release candidate of the 1.2.0.RELEASE, based on the 1.2.x branch. One of the most important
changes in this release is that we use events in the auditing domain now instead of strings. In addition we've improved
the startup time and memory usage of the OpenEngSB. One really nice improvement in this release is that we allow OSGi-
filters in remote-calls in addition to the serviceId, required till now. In addition we've fixed various bugs, found in
the previous release candidate, in remoting and provisioning. An additional important change was the change back to
Apache Felix from Eclipse Equinox as default runtime environment. Besides the typical documentation improvements we've
also upgraded 17 libraries including CXF (2.4.1), OpenEngSB Root (18) and JGit (0.12.1).

### Details
** Bug
    * [OPENENGSB-1594] - JsonParseException: Unexpected character
    * [OPENENGSB-1599] - ports-ws does not create a correct webservice wsdl
    * [OPENENGSB-1602] - openengsb:provision does not start because of permission problem
    * [OPENENGSB-1616] - README does not reflect the current state-of-the-art
    * [OPENENGSB-1617] - NOTICE file should not be executable
    * [OPENENGSB-1618] - connector-archetype should use separate domain-version
    * [OPENENGSB-1662] - itests fail because of config-persistence-setup-issue
    * [OPENENGSB-1671] - Ports NullPointerException in case of a Void method
    * [OPENENGSB-1675] - ConnectorDescription: Use of obsolete class "Dictionary"
    * [OPENENGSB-1694] - update documentation about connector-config-files
    * [OPENENGSB-1714] - Features.xml still contains auditing connector & domain feature

** Improvement
    * [OPENENGSB-1600] - Start JMS ports modules only when required
    * [OPENENGSB-1601] - Couple wicket to ui-common feature instead to core common
    * [OPENENGSB-1619] - Reference to raw changelog instead of formatted on HP
    * [OPENENGSB-1658] - allow osgi-filters in remote-calls instead of just service-ids
    * [OPENENGSB-1718] - Comment in AbstractExamTestHelper about debug/log is not resistent about formatting

** Library Upgrade
    * [OPENENGSB-1621] - Upgrade to openengsb-root-18
    * [OPENENGSB-1698] - Upgrade xmlsec to 1.4.5_1
    * [OPENENGSB-1699] - Upgrade cxf to 2.4.1
    * [OPENENGSB-1700] - Upgrade jaxb to 1.8.0
    * [OPENENGSB-1701] - Upgrade JSR311 to 1.8.0
    * [OPENENGSB-1702] - Upgrade jaxws to 1.8.0
    * [OPENENGSB-1703] - Upgrade saaj to 1.8.0
    * [OPENENGSB-1704] - Upgrade asm to 3.3_2
    * [OPENENGSB-1705] - Upgrade wss4j to 1.6.1
    * [OPENENGSB-1706] - Upgrade stax to 1.8.0
    * [OPENENGSB-1707] - Upgrade activation to 1.8.0
    * [OPENENGSB-1713] - Upgrade xjc to 2.2.1.1_2
    * [OPENENGSB-1730] - Upgrade jaxb to 2.2.3
    * [OPENENGSB-1731] - Upgrade jackson libs to 1.8.2
    * [OPENENGSB-1732] - Upgrade jgit to 0.12.1
    * [OPENENGSB-1733] - Upgrade geronimo servlet to 1.2
    * [OPENENGSB-1734] - Upgrade javax mail api to 1.4.1_4

** New Feature
    * [OPENENGSB-1690] - use events instead of strings in auditingdomain

** Task
    * [OPENENGSB-1329] - Create Demo-scenario for humantasks
    * [OPENENGSB-1590] - Release openengsb-1.2.0.RC3
    * [OPENENGSB-1646] - Upgrade submodules to latest clienprojects
    * [OPENENGSB-1659] - create alterntive run-script without openengsb:provision
    * [OPENENGSB-1661] - update howto about installing domains and connectors
    * [OPENENGSB-1665] - fix warnings in openengsb project
    * [OPENENGSB-1697] - Switch back to Apache Felix


openengsb-framework-1.2.0.RC2 2011-05-16
--------------------------------------------

Upgrade karaf to 2.2.1, fix various bugs in json, remoting, classloading, jms integration tests and increase 
documentation and examples.

### Highlights
  * Upgrade to karaf-2.2.1
  * Completely re-wrote c# examples
  * Increase documentation & fix documentation bugs
  * Fix jms integration tests
  * Fix remoting problems

### Details
** Bug
    * [OPENENGSB-1212] - JMS Integration tests fail
    * [OPENENGSB-1244] - Create config-factories via features <config> tag
    * [OPENENGSB-1351] - JMS does not "survive" a second start
    * [OPENENGSB-1361] - Domains could not be called on testclient
    * [OPENENGSB-1408] - Default registered auditing service does not follow naming specification
    * [OPENENGSB-1412] - JSON object marshaller does not accept annotations
    * [OPENENGSB-1414] - DefaultRequestHandler uses current classes instead of transfared to find method
    * [OPENENGSB-1419] - memory-auditing-connectors always return the same instanceid ("auditing")
    * [OPENENGSB-1422] - auditing-domain-provider is not exported with the correct properties
    * [OPENENGSB-1469] - using connector-deployer with no attributes breaks Neodatis-persistence
    * [OPENENGSB-1471] - maven-tidy plugin invalidates xhtml by line-wrapping quickfix
    * [OPENENGSB-1472] - Error in JSON sample in the Documentation of the nightly build chp 6.1.1.1
    * [OPENENGSB-1473] - Messages enqueued to "receive" are automatically dequeued
    * [OPENENGSB-1476] - maven-tidy plugin invalidates xhtml by line-wrapping
    * [OPENENGSB-1477] - manual images are not shown on homepage
    * [OPENENGSB-1481] - OpenEngSB Wrapped Dependencies have to be defined in poms/pom.xml instead of root pom
    * [OPENENGSB-1513] - package name is not considered when reloading rulebase
    * [OPENENGSB-1514] - CI tutorial sample project link is invalid
    * [OPENENGSB-1522] - AuthenticationCredentialsNotFoundException thrown on jms call handle
    * [OPENENGSB-1530] - mvn install build failure under windows
    * [OPENENGSB-1533] - Licenses in all nmsprovider-activemq.config are not correct
    * [OPENENGSB-1535] - OpenEngSB versions are generated wrong for connectors/domains
    * [OPENENGSB-1537] - Not all submodules in .gitmodule define urls the same schema
    * [OPENENGSB-1566] - bundle-info patch broke infrastructure-jms
    * [OPENENGSB-1569] - Classloading in JMS Ports is messed

** Improvement
    * [OPENENGSB-1323] - sort imports in import-managing-UI
    * [OPENENGSB-1389] - Move Quickstart section into howto section of manual and link it
    * [OPENENGSB-1407] - Make it possible to configure queues on localhost for jms-json remote
    * [OPENENGSB-1416] - JSON responses does not contains sent information
    * [OPENENGSB-1480] - Add aspectj also to dependency section
    * [OPENENGSB-1511] - Include into maintenance faq that this policy apply to all components
    * [OPENENGSB-1538] - Update recommended eclipse plugins
    * [OPENENGSB-1556] - Increase default memory used with karaf
    * [OPENENGSB-1567] - Add bundle-info template to connector/domain/client project archetype

** Library Upgrade
    * [OPENENGSB-1272] - Upgrade to karaf-2.2.1
    * [OPENENGSB-1413] - Upgrade to jackson 1.8.0
    * [OPENENGSB-1545] - Upgrade aries blueprint to 0.3.1
    * [OPENENGSB-1555] - Upgrade to openengsb-root-16
    * [OPENENGSB-1582] - Upgrade to openengsb-root-17

** New Feature
    * [OPENENGSB-948] - Add OSGI-INF/bundle.info as used in Karaf to the openengsb bundles
    * [OPENENGSB-1292] - create wiring-page (to replace context-editor-page)
    * [OPENENGSB-1534] - Add openengsb-connector csharp example

** Task
    * [OPENENGSB-1089] - Update dev documentation to reflect that each commit should include [OPENENGSB-ISSUEID] in its message
    * [OPENENGSB-1350] - Upgrade all submodules to the finally released connectors with the release
    * [OPENENGSB-1362] - Do not startup openengsb-ports-jms in framework distribution per default
    * [OPENENGSB-1402] - release openengsb-1.2.0.RC2
    * [OPENENGSB-1507] - Push connector and domain submodules to latest version in features
    * [OPENENGSB-1509] - For the submodules pull AND push urls should be specified
    * [OPENENGSB-1532] - Integrate OpenEngSB with http://www.ohloh.net/


openengsb-framework-1.2.0.RC1 2011-04-26
--------------------------------------------

Stabilized openengsb-1.2.0.M5 version prepared for first final release of openengsb-1.2.0.RELEASE

### Highlights
  * Remoting fully functional again
  * Remote connections allowed to amq
  * Show version name in console
  * Allow to overwrite call router

### Details
** Bug
    * [OPENENGSB-1333] - ConnectorDeployer should not overwrite changes made outside of the files
    * [OPENENGSB-1344] - ConfigPersistence is retrieved too early if retrieved as instance variable
    * [OPENENGSB-1346] - ports-ws does not provide a method in the WS
    * [OPENENGSB-1347] - OpenEngSB does not start ports-ws if ports-jms is started during ui-admin
    * [OPENENGSB-1360] - Delete and call buttons on testclient page do not get deactivated during execution
    * [OPENENGSB-1392] - Remote connection to AMQ returns connection refused
    * [OPENENGSB-1393] - Classpath loading exception with cxf ws json service
    * [OPENENGSB-1400] - Variables in pom.xml files are not replaced during build/release
    * [OPENENGSB-1404] - Referencing serviceUtils in bundle exporting it have to be done via blueprint
    * [OPENENGSB-1406] - CallRouter could not be retrieved

** Improvement
    * [OPENENGSB-1390] - Documentation howto.remote talks about Rules although processes are meant

** Library Upgrade
    * [OPENENGSB-1355] - upgrade to openengsb-root-15

** New Feature
    * [OPENENGSB-1403] - Show openengsb name in karaf console branding

** Task
    * [OPENENGSB-960] - Document support strategy on hp
    * [OPENENGSB-1349] - release openengsb-1.2.0.RC1


openengsb-framework-1.2.0.M5 2011-04-21
--------------------------------------------

This milestone release contains various improvements and bugfixes. In addition there where many complete 
rewrites in the src and API base. The old core-common project had been split into core-api, core-common 
and core-services. In addition all connectors and domains had been split from the OpenEngSB. A new 
configuration backend had been added and the admin UI had been upgraded and enhanced at various places. 
We're using CXF 2.4 and AMQ 5.5 now including their own features files. Finally the entire ServiceManager 
architecture had been rewritten using only one central manager now. In addition we've invested much time 
enhancing the documentation and including new howtos.

### Highlights
  * Documentation upgrade
  * Central ConnectorManager replace multible service managers
  * New config-persistence allows to store configurations at various places now
  * Connectors and context can be created as files
  * Admin UI can edit properties of services now
  * CXF 2.4 and AMQ 5.5 had been included
  * ports-ws allows "ports-integration" with the OpenEngSB via WS now

### Details
** Bug
    * [OPENENGSB-1049] - Testclient does no longer allow to call via domains
    * [OPENENGSB-1106] - AbstractOpenEngSBInvocationHandler cannot invoke method called "notify"
    * [OPENENGSB-1122] - updateService in AbstractServiceManager allows inconsistent IDs
    * [OPENENGSB-1130] - allow sending emails without attachment
    * [OPENENGSB-1134] - Exception when starting ssh-server
    * [OPENENGSB-1170] - archetype connector create invalid blueprint xml file
    * [OPENENGSB-1185] - integration-tests should use different ports than the distribution
    * [OPENENGSB-1195] - Connector archetype create ServiceManager with wrong constructor
    * [OPENENGSB-1207] - itests print DEBUG-output
    * [OPENENGSB-1210] - Git submodules for connectors does not work without commit rights
    * [OPENENGSB-1214] - OpenEngSB Version object should be serializable
    * [OPENENGSB-1215] - Correct build-order of modules
    * [OPENENGSB-1218] - testclient throws for every service which is called a nullpointer exception
    * [OPENENGSB-1221] - serialization problem when call service in testclient
    * [OPENENGSB-1228] - feature to add globals via web UI
    * [OPENENGSB-1229] - feature to add imports for DomainEndpointFactory
    * [OPENENGSB-1230] - notification of rule persistence failure
    * [OPENENGSB-1231] - WorkflowService has to extend OpenEngSBService
    * [OPENENGSB-1234] - All Dependencies for integration tests have to be provided with main repo
    * [OPENENGSB-1258] - ProxyConnector also allows Void as return value
    * [OPENENGSB-1260] - Error in JSON sample in the Documentation
    * [OPENENGSB-1287] - client-project archetype does not work as expected
    * [OPENENGSB-1291] - example-connector does not use config-attribute prefix.
    * [OPENENGSB-1321] - eliminate null values in testclient service list
    * [OPENENGSB-1335] - TestClient adaptions had not been included into unit tests

** Epic
    * [OPENENGSB-572] - plugin-system for openengsb-based projects
    * [OPENENGSB-590] - restructure documentation (again)
    * [OPENENGSB-812] - Provide port for webservices via CXF
    * [OPENENGSB-878] - Configure openengsb parts in configuration folder
    * [OPENENGSB-1005] - OpenEngSB PIM suite functionality

** Improvement
    * [OPENENGSB-562] - Quickstart documentation is practically empty
    * [OPENENGSB-903] - Use karaf.shell name property to openengsb
    * [OPENENGSB-993] - extend ServiceManager to be able to manage locations
    * [OPENENGSB-1070] - cancel workflow and all associated (open) tasks
    * [OPENENGSB-1100] - extract jira-plugin to separate repository
    * [OPENENGSB-1112] - match filters properly in AbstractOsgiServiceMockTest
    * [OPENENGSB-1113] - split core-common into common and api
    * [OPENENGSB-1129] - Step-by-step tutorial  to building CI environment with OpenEngSB
    * [OPENENGSB-1148] - waitForFlowToFinish should indicate if the flow is finished.
    * [OPENENGSB-1162] - Allow to configure all different smtp endpoints
    * [OPENENGSB-1174] - merge taskbox and workflow in core-api into workflow package
    * [OPENENGSB-1176] - Merge api.workflow.editor with api.workflow
    * [OPENENGSB-1178] - Merge connectorsetupstore namespace into persistence
    * [OPENENGSB-1182] - Make all classes in deployer internal
    * [OPENENGSB-1186] - make jms-url configurable 
    * [OPENENGSB-1192] - UI-Admin should not reference auditing connector but rather domain
    * [OPENENGSB-1193] - tasboxService.update(Task task)
    * [OPENENGSB-1199] - Use system.properties, config.properties and jre.properties directly
    * [OPENENGSB-1202] - Workflow exceptions should be untagged
    * [OPENENGSB-1203] - Taskbox exception should extend workflow exception
    * [OPENENGSB-1204] - Move taskbox into internal in workflow project
    * [OPENENGSB-1220] - Include submodule connectors/wrapped via pom file in openengsb
    * [OPENENGSB-1233] - ability to remove rules from the rule base
    * [OPENENGSB-1247] - Move openengsb-dependency management documentation to own client section
    * [OPENENGSB-1254] - Allow to register multible Services with different IDs
    * [OPENENGSB-1259] - Move CallRouter from core-api to core-services
    * [OPENENGSB-1261] - Cleanup JRE file to contain only non-exported dependencies
    * [OPENENGSB-1262] - Use smx jaxb bundles instead of own
    * [OPENENGSB-1263] - Cleanup ports-jms feature
    * [OPENENGSB-1267] - Client & Shell Script Missing in "Pink Panther"
    * [OPENENGSB-1283] - deleting of connectors
    * [OPENENGSB-1308] - Allow all services to be called in call-router
    * [OPENENGSB-1313] - Move osgi property id to constants
    * [OPENENGSB-1318] - Prevent non-required karaf default features from startup

** Library Upgrade
    * [OPENENGSB-1033] - Upgrade activemq to 5.5
    * [OPENENGSB-1200] - Upgrade to openengsb-root-14
    * [OPENENGSB-1315] - Upgrade jackson libs to 1.7.6
    * [OPENENGSB-1316] - Upgrade commons-lang to 2.6

** New Feature
    * [OPENENGSB-728] - Create context via configuration files
    * [OPENENGSB-910] - When creating connector via web-ui set location properties
    * [OPENENGSB-1032] - User ActiveMQ Features for Broker creation instead of own
    * [OPENENGSB-1101] - Export Workflow containing actions and events to WorkflowService
    * [OPENENGSB-1141] - implement gcontacts connector for domain contact
    * [OPENENGSB-1142] - implement gcalendar connector for domain appointment
    * [OPENENGSB-1165] - Create separated Config-Persistence
    * [OPENENGSB-1168] - Configure activemq webapp
    * [OPENENGSB-1171] - Add static OpenEngSB class to core-common retrieving openengsb core services in a static way
    * [OPENENGSB-1172] - create subproject for wrapping com.google.gdata
    * [OPENENGSB-1173] - wrapping com.google.gdata-calendar
    * [OPENENGSB-1198] - wrapping com.google.gdata-contacts
    * [OPENENGSB-1216] - Add a general testing feature for OpenEngSBCoreServices
    * [OPENENGSB-1246] - Use a centralized Constants class
    * [OPENENGSB-1251] - Provide Connector-Specific ConfigPersistenceImplementation
    * [OPENENGSB-1252] - provide context configpersistence implementation
    * [OPENENGSB-1256] - add delete-method to config-persistence
    * [OPENENGSB-1264] - Provide cxf-feature in openengsb

** Task
    * [OPENENGSB-280] - move connectors and domains out of main openengsb repo
    * [OPENENGSB-655] - Document howto use client projects with the openengsb
    * [OPENENGSB-656] - Create archetypes for client projects
    * [OPENENGSB-658] - update archetypes for domains and connectors for distributed projects
    * [OPENENGSB-698] - Create integration tests for taskbox panel functionality
    * [OPENENGSB-916] - Use slf4j instead of commons-logging althrough
    * [OPENENGSB-998] - use ranking of -1 for root-services
    * [OPENENGSB-1083] - Create Connector Provider
    * [OPENENGSB-1084] - Rename DomainEndpointFactory to Wiring
    * [OPENENGSB-1087] - Back DomainEndpointFactory by a service
    * [OPENENGSB-1097] - release openengsb-1.2.0.M5
    * [OPENENGSB-1098] - Define release name using P P
    * [OPENENGSB-1128] - remove DomainService from core-common
    * [OPENENGSB-1133] - remove Feedback-panels from all custom panels
    * [OPENENGSB-1175] - Remove OpenEngSB BundleContextAware interface
    * [OPENENGSB-1177] - Rename workflow.editor.model classes with end Representation*
    * [OPENENGSB-1179] - Move DomainService into root
    * [OPENENGSB-1180] - Merge current proxy and communication namespace in api.remote
    * [OPENENGSB-1181] - merge core-events into core-common
    * [OPENENGSB-1183] - remove remainging references to openengsb-core-events
    * [OPENENGSB-1191] - Remove no longer required spring dependencies from compiled pom.xml
    * [OPENENGSB-1196] - rename subproject openengsb-wrapped-com.google.gdata in openengsb-wrapped-com.google.gdata-calendar
    * [OPENENGSB-1197] - create subproject for wrapping com.google.gdata-contacts
    * [OPENENGSB-1201] - Create CSS configuration for task overview. 
    * [OPENENGSB-1209] - Rename poms/compiled to poms/bundles
    * [OPENENGSB-1211] - Remove direct reference to memoryauditing connector
    * [OPENENGSB-1213] - Reevaluate how to handle hard to test WiringService in unittests
    * [OPENENGSB-1224] - Merge openengsb-core-deployer-connector into openengsb-core-services
    * [OPENENGSB-1236] - create parent pom for domains
    * [OPENENGSB-1237] - move default-connectors back in openengsb-repo
    * [OPENENGSB-1238] - move non-essential domains to separate repos
    * [OPENENGSB-1239] - move external features to own features-directory
    * [OPENENGSB-1240] - remove connector-feature-urls from openengsb-feature-file
    * [OPENENGSB-1257] - Remove core/services/src/main/java/org/openengsb/core/services/internal/InvocationHandlerFactory.java
    * [OPENENGSB-1288] - remove context-editor-page from ui-admin
    * [OPENENGSB-1317] - adapt ConnectorDeployer to new ServiceManager
    * [OPENENGSB-1320] - in the administration ui documentation is the part for managing globals missing
    * [OPENENGSB-1324] - update conenctor archetype to new servicemanager api
    * [OPENENGSB-1325] - Remove submodules wrapped from openengsb
    * [OPENENGSB-1337] - Add bundles to download page


openengsb-framework-1.2.0.M4 2011-03-23
--------------------------------------------

This milestone release contains various improvements and bugfixes. In addition the features packaing and the 
distribution has been completely refactored. In addition two new domains had been added and the exception model had 
been adapted.

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


openengsb-framework-1.2.0.M3 2011-03-09
--------------------------------------------

This milestone release contains many improvements in the manual and on the hompage and an upgrade and some upgrades to 
some core concepts. In addition many libraries where upgraded. You should really scan the detailed list to find if and 
mostly where this release affects you.

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


openengsb-framework-1.2.0.M2 2011-02-23
--------------------------------------------

Alhough a milestone release it only contains small changes with an upgrade to karaf-2.1.4, upgrade to equinox instead 
of felix and some minor enhancements and bugfixes.

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


openengsb-framework-1.2.0.M1 2011-02-09
--------------------------------------------

Besides many bugfixes and minor improvements this release contains 9 dependency upgrades, the extraction of the 
openengsb-maven-plugin and the possibility to create connectors from configuration files.

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
